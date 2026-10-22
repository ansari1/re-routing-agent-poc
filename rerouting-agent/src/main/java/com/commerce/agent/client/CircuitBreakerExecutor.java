package com.commerce.agent.client;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.commerce.common.error.UpstreamUnavailableException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

/** Runs a remote call through a named circuit breaker and normalises transport failures. */
@Component
public class CircuitBreakerExecutor {

    private final CircuitBreakerRegistry registry;

    public CircuitBreakerExecutor(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }

    public <T> T execute(String breakerName, String configName, Supplier<T> call,
                         Function<CallNotPermittedException, ? extends RuntimeException> whenOpen) {
        var breaker = registry.circuitBreaker(breakerName, configName);
        try {
            return breaker.executeSupplier(() -> translateTransportErrors(breakerName, call));
        } catch (CallNotPermittedException open) {
            throw whenOpen.apply(open);
        }
    }

    public <T> T execute(String serviceName, Supplier<T> call) {
        return execute(serviceName, "default", call, open -> new UpstreamUnavailableException(serviceName, open));
    }

    private static <T> T translateTransportErrors(String service, Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientException e) {
            throw new UpstreamUnavailableException(service, e);
        }
    }
}
