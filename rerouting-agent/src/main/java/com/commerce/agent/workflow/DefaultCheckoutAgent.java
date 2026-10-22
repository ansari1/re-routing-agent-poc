package com.commerce.agent.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.commerce.agent.api.CheckoutOutcome;
import com.commerce.agent.api.CheckoutRequest;
import com.commerce.agent.api.CheckoutResult;
import com.commerce.agent.client.InventoryGateway;
import com.commerce.agent.client.OrderGateway;
import com.commerce.agent.client.PaymentGateway;
import com.commerce.common.contract.InventoryContracts.AvailabilityRequest;
import com.commerce.common.contract.InventoryContracts.ReservationRequest;
import com.commerce.common.contract.InventoryContracts.WarehouseView;
import com.commerce.common.contract.OrderContracts.CreateOrderRequest;
import com.commerce.common.contract.OrderContracts.OrderItem;
import com.commerce.common.contract.OrderContracts.OrderStatus;
import com.commerce.common.contract.OrderContracts.OrderView;
import com.commerce.common.contract.OrderContracts.UpdateOrderStatusRequest;
import com.commerce.common.contract.PaymentContracts.PaymentRequest;
import com.commerce.common.contract.PaymentContracts.PaymentStatus;

/** Runs the whole purchase: order, stock, payment, shipment, and autonomous recovery from delivery delays. */
@Service
public class DefaultCheckoutAgent implements CheckoutAgent {

    private final OrderGateway orders;
    private final InventoryGateway inventory;
    private final PaymentGateway payments;
    private final ShipmentCoordinator shipping;
    private final CustomerNotifier notifier;

    public DefaultCheckoutAgent(OrderGateway orders, InventoryGateway inventory, PaymentGateway payments,
                                ShipmentCoordinator shipping, CustomerNotifier notifier) {
        this.orders = orders;
        this.inventory = inventory;
        this.payments = payments;
        this.shipping = shipping;
        this.notifier = notifier;
    }

    @Override
    public CheckoutResult checkout(CheckoutRequest request) {
        var timeline = new ArrayList<String>();
        var stock = request.items().stream().map(OrderItem::toStockLine).toList();

        var order = orders.create(new CreateOrderRequest(request.customerId(), request.destination(), request.items()));
        timeline.add("Order %d created".formatted(order.id()));

        var options = inventory.findAvailable(new AvailabilityRequest(stock, Set.of()));
        if (options.isEmpty()) {
            return cancel(order, CheckoutOutcome.OUT_OF_STOCK, "no warehouse has the requested stock", timeline);
        }
        var primary = options.get(0);
        inventory.reserve(new ReservationRequest(order.id(), primary.id(), stock));
        timeline.add("Stock reserved at %s".formatted(primary.code()));

        var payment = payments.pay(new PaymentRequest(order.id(), order.total(), request.paymentMethod()));
        if (payment.status() != PaymentStatus.AUTHORIZED) {
            inventory.release(order.id());
            return cancel(order, CheckoutOutcome.PAYMENT_DECLINED, "payment was declined", timeline);
        }
        var paidOrder = setStatus(order, OrderStatus.PAID, primary.id());
        timeline.add("Payment authorised");

        return shipping.ship(paidOrder, primary, timeline)
                .map(dispatch -> shipped(paidOrder, primary, dispatch, timeline))
                .orElseGet(() -> delayed(paidOrder, primary, timeline));
    }

    private CheckoutResult shipped(OrderView order, WarehouseView primary, Dispatch dispatch, List<String> timeline) {
        var warehouse = dispatch.warehouse();
        var tracking = dispatch.shipment().trackingCode();
        OrderView updated;
        CheckoutOutcome outcome;
        String message;
        if (dispatch.rerouted()) {
            setStatus(order, OrderStatus.DELIVERY_DELAYED, primary.id());
            updated = setStatus(order, OrderStatus.REROUTED, warehouse.id());
            updated = setStatus(updated, OrderStatus.SHIPPED, warehouse.id());
            outcome = CheckoutOutcome.REROUTED;
            message = NotificationTemplate.ORDER_REROUTED.render(order.id(), primary.region(), warehouse.code());
        } else {
            updated = setStatus(order, OrderStatus.SHIPPED, warehouse.id());
            outcome = CheckoutOutcome.COMPLETED;
            message = NotificationTemplate.ORDER_CONFIRMED.render(order.id(), warehouse.code());
        }
        timeline.add("Shipment booked from %s, tracking %s".formatted(warehouse.code(), tracking));
        notifier.notify(order.customerId(), order.id(), message);
        return new CheckoutResult(updated, outcome, warehouse, tracking, List.copyOf(timeline));
    }

    private CheckoutResult delayed(OrderView order, WarehouseView primary, List<String> timeline) {
        var updated = setStatus(order, OrderStatus.DELIVERY_DELAYED, primary.id());
        notifier.notify(order.customerId(), order.id(),
                NotificationTemplate.ORDER_DELAYED.render(order.id(), "no warehouse can ship at the moment"));
        return new CheckoutResult(updated, CheckoutOutcome.DELIVERY_DELAYED, primary, null, List.copyOf(timeline));
    }

    private CheckoutResult cancel(OrderView order, CheckoutOutcome outcome, String reason, List<String> timeline) {
        var cancelled = setStatus(order, OrderStatus.CANCELLED, null);
        timeline.add("Order cancelled: " + reason);
        notifier.notify(order.customerId(), order.id(), NotificationTemplate.ORDER_CANCELLED.render(order.id(), reason));
        return new CheckoutResult(cancelled, outcome, null, null, List.copyOf(timeline));
    }

    private OrderView setStatus(OrderView order, OrderStatus status, Long warehouseId) {
        return orders.updateStatus(order.id(), new UpdateOrderStatusRequest(status, warehouseId));
    }
}
