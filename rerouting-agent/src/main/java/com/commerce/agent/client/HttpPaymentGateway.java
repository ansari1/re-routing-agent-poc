package com.commerce.agent.client;

import org.springframework.stereotype.Component;

import com.commerce.agent.config.ServicesProperties;
import com.commerce.common.contract.PaymentContracts.PaymentRequest;
import com.commerce.common.contract.PaymentContracts.PaymentView;

@Component
class HttpPaymentGateway extends RemoteGateway implements PaymentGateway {

    HttpPaymentGateway(ServicesProperties props, RestClientFactory clients, CircuitBreakerExecutor breaker) {
        super("payment-service", props.paymentUrl(), clients, breaker);
    }

    @Override
    public PaymentView pay(PaymentRequest request) {
        return call(() -> client.post().uri("/api/v1/payments").body(request).retrieve().body(PaymentView.class));
    }
}
