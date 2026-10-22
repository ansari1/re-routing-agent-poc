package com.commerce.agent.workflow;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.commerce.agent.client.DeliveryGateway;
import com.commerce.agent.client.InventoryGateway;
import com.commerce.agent.config.AgentProperties;
import com.commerce.agent.routing.RouteAdvisor;
import com.commerce.agent.routing.RouteContext;
import com.commerce.common.contract.DeliveryContracts.ShipmentRequest;
import com.commerce.common.contract.DeliveryContracts.ShipmentView;
import com.commerce.common.contract.InventoryContracts.AvailabilityRequest;
import com.commerce.common.contract.InventoryContracts.ReservationRequest;
import com.commerce.common.contract.InventoryContracts.StockLine;
import com.commerce.common.contract.InventoryContracts.WarehouseView;
import com.commerce.common.contract.OrderContracts.OrderView;
import com.commerce.common.error.BusinessRuleException;
import com.commerce.common.error.DeliveryDelayedException;
import com.commerce.common.error.DomainException;
import com.commerce.common.error.NotFoundException;
import com.commerce.common.error.UpstreamUnavailableException;

/**
 * Books a shipment and, when the origin is delayed, autonomously moves the order to another warehouse.
 * The order's stock reservation always follows the warehouse that is currently responsible for it.
 */
@Component
public class ShipmentCoordinator {

    private static final Logger log = LoggerFactory.getLogger(ShipmentCoordinator.class);

    private final DeliveryGateway delivery;
    private final InventoryGateway inventory;
    private final RouteAdvisor advisor;
    private final AgentProperties properties;

    public ShipmentCoordinator(DeliveryGateway delivery, InventoryGateway inventory, RouteAdvisor advisor,
                               AgentProperties properties) {
        this.delivery = delivery;
        this.inventory = inventory;
        this.advisor = advisor;
        this.properties = properties;
    }

    /** @return the dispatch, or empty when no warehouse can ship right now. */
    public Optional<Dispatch> ship(OrderView order, WarehouseView primary, List<String> timeline) {
        try {
            return Optional.of(new Dispatch(primary, book(order, primary), false));
        } catch (DeliveryDelayedException delayed) {
            timeline.add("Delivery from %s delayed: %s".formatted(primary.code(), delayed.getMessage()));
            return reroute(order, primary, delayed.getMessage(), timeline);
        } catch (UpstreamUnavailableException down) {
            timeline.add("Delivery service unavailable; re-routing cannot help");
            return Optional.empty();
        }
    }

    private Optional<Dispatch> reroute(OrderView order, WarehouseView origin, String reason, List<String> timeline) {
        var stock = order.items().stream().map(item -> item.toStockLine()).toList();
        var excludedRegions = new HashSet<String>();
        var holding = origin;
        var blocked = origin;

        for (int attempt = 1; attempt <= properties.maxRerouteAttempts(); attempt++) {
            excludedRegions.add(blocked.region());
            var candidates = inventory.findAvailable(new AvailabilityRequest(stock, excludedRegions));
            if (candidates.isEmpty()) {
                timeline.add("No alternative warehouse can fulfil the order");
                return Optional.empty();
            }

            var choice = advisor.choose(new RouteContext(order, blocked, reason, candidates));
            timeline.add("Agent (%s) selected warehouse %s in %s".formatted(advisor.name(), choice.code(), choice.region()));

            try {
                moveReservation(order, holding, choice, stock);
                holding = choice;
                return Optional.of(new Dispatch(choice, book(order, choice), true));
            } catch (DeliveryDelayedException delayed) {
                timeline.add("Delivery from %s delayed too: %s".formatted(choice.code(), delayed.getMessage()));
                blocked = choice;
                reason = delayed.getMessage();
            } catch (BusinessRuleException stockRace) {
                timeline.add("Warehouse %s could not reserve stock: %s".formatted(choice.code(), stockRace.getMessage()));
                blocked = choice;
            } catch (UpstreamUnavailableException down) {
                timeline.add("A required service became unavailable during re-routing");
                return Optional.empty();
            }
        }
        timeline.add("Re-routing attempts exhausted");
        return Optional.empty();
    }

    private void moveReservation(OrderView order, WarehouseView from, WarehouseView to, List<StockLine> stock) {
        if (from.id().equals(to.id())) {
            return;
        }
        try {
            inventory.release(order.id());
        } catch (NotFoundException noActiveReservation) {
            log.debug("Order {} had no active reservation to release", order.id());
        }
        try {
            inventory.reserve(new ReservationRequest(order.id(), to.id(), stock));
        } catch (DomainException failure) {
            restoreQuietly(order, from, stock);
            throw failure;
        }
    }

    private void restoreQuietly(OrderView order, WarehouseView warehouse, List<StockLine> stock) {
        try {
            inventory.reserve(new ReservationRequest(order.id(), warehouse.id(), stock));
        } catch (DomainException e) {
            log.warn("Could not restore reservation of order {} at {}: {}", order.id(), warehouse.code(), e.getMessage());
        }
    }

    private ShipmentView book(OrderView order, WarehouseView warehouse) {
        return delivery.book(new ShipmentRequest(order.id(), warehouse.id(), warehouse.region(), order.destination()));
    }
}
