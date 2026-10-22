package com.commerce.agent.client;

import java.util.function.Supplier;

import org.springframework.web.client.RestClient;

/** Shared plumbing for gateways: a configured client plus circuit-breaker execution. */
abstract class RemoteGateway {

    protected final RestClient client;
    private final String serviceName;
    private final CircuitBreakerExecutor breaker;

    protected RemoteGateway(String serviceName, String baseUrl, RestClientFactory clients,
                            CircuitBreakerExecutor breaker) {
        this.serviceName = serviceName;
        this.client = clients.create(serviceName, baseUrl);
        this.breaker = breaker;
    }

    protected <T> T call(Supplier<T> call) {
        return breaker.execute(serviceName, call);
    }
}
