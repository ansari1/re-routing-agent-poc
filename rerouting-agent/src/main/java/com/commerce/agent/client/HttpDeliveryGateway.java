package com.commerce.agent.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.commerce.agent.config.ServicesProperties;
import com.commerce.common.contract.DeliveryContracts.ShipmentRequest;
import com.commerce.common.contract.DeliveryContracts.ShipmentView;
import com.commerce.common.error.DeliveryDelayedException;

/**
 * One circuit breaker per origin region: a weather-blocked region opens only its own breaker,
 * so re-routed bookings from healthy regions keep flowing.
 */
@Component
class HttpDeliveryGateway implements DeliveryGateway {

    private static final String SERVICE = "delivery-service";

    private final RestClient client;
    private final CircuitBreakerExecutor breaker;

    HttpDeliveryGateway(ServicesProperties props, RestClientFactory clients, CircuitBreakerExecutor breaker) {
        this.client = clients.create(SERVICE, props.deliveryUrl());
        this.breaker = breaker;
    }

    @Override
    public ShipmentView book(ShipmentRequest request) {
        var region = request.warehouseRegion();
        return breaker.execute(
                "%s-%s".formatted(SERVICE, region),
                "delivery",
                () -> client.post().uri("/api/v1/shipments").body(request).retrieve().body(ShipmentView.class),
                open -> new DeliveryDelayedException("Deliveries from %s are paused (circuit open)".formatted(region), open));
    }
}
