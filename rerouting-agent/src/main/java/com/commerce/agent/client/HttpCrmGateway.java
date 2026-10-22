package com.commerce.agent.client;

import org.springframework.stereotype.Component;

import com.commerce.agent.config.ServicesProperties;
import com.commerce.common.contract.CrmContracts.NotificationRequest;

@Component
class HttpCrmGateway extends RemoteGateway implements CrmGateway {

    HttpCrmGateway(ServicesProperties props, RestClientFactory clients, CircuitBreakerExecutor breaker) {
        super("crm-service", props.crmUrl(), clients, breaker);
    }

    @Override
    public void notifyCustomer(NotificationRequest request) {
        call(() -> client.post().uri("/api/v1/crm/notifications").body(request).retrieve().toBodilessEntity());
    }
}
