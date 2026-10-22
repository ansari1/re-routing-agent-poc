package com.commerce.agent.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import com.commerce.agent.config.ServicesProperties;
import com.commerce.common.contract.InventoryContracts.AvailabilityRequest;
import com.commerce.common.contract.InventoryContracts.ReservationRequest;
import com.commerce.common.contract.InventoryContracts.ReservationView;
import com.commerce.common.contract.InventoryContracts.WarehouseView;

@Component
class HttpInventoryGateway extends RemoteGateway implements InventoryGateway {

    private static final ParameterizedTypeReference<List<WarehouseView>> WAREHOUSES = new ParameterizedTypeReference<>() { };

    HttpInventoryGateway(ServicesProperties props, RestClientFactory clients, CircuitBreakerExecutor breaker) {
        super("inventory-service", props.inventoryUrl(), clients, breaker);
    }

    @Override
    public List<WarehouseView> findAvailable(AvailabilityRequest request) {
        return call(() -> client.post().uri("/api/v1/inventory/availability")
                .body(request).retrieve().body(WAREHOUSES));
    }

    @Override
    public ReservationView reserve(ReservationRequest request) {
        return call(() -> client.post().uri("/api/v1/inventory/reservations")
                .body(request).retrieve().body(ReservationView.class));
    }

    @Override
    public ReservationView release(Long orderId) {
        return call(() -> client.delete().uri("/api/v1/inventory/reservations/{orderId}", orderId)
                .retrieve().body(ReservationView.class));
    }
}
