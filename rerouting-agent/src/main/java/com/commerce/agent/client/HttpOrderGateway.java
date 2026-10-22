package com.commerce.agent.client;

import org.springframework.stereotype.Component;

import com.commerce.agent.config.ServicesProperties;
import com.commerce.common.contract.OrderContracts.CreateOrderRequest;
import com.commerce.common.contract.OrderContracts.OrderView;
import com.commerce.common.contract.OrderContracts.UpdateOrderStatusRequest;

@Component
class HttpOrderGateway extends RemoteGateway implements OrderGateway {

    HttpOrderGateway(ServicesProperties props, RestClientFactory clients, CircuitBreakerExecutor breaker) {
        super("order-service", props.orderUrl(), clients, breaker);
    }

    @Override
    public OrderView create(CreateOrderRequest request) {
        return call(() -> client.post().uri("/api/v1/orders").body(request).retrieve().body(OrderView.class));
    }

    @Override
    public OrderView updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        return call(() -> client.patch().uri("/api/v1/orders/{id}/status", orderId)
                .body(request).retrieve().body(OrderView.class));
    }
}
