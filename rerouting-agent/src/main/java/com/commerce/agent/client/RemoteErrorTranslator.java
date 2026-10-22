package com.commerce.agent.client;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.commerce.common.error.ApiError;
import com.commerce.common.error.BusinessRuleException;
import com.commerce.common.error.DeliveryDelayedException;
import com.commerce.common.error.NotFoundException;
import com.commerce.common.error.UpstreamUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Turns the platform's uniform error payload back into the matching domain exception. */
@Component
public class RemoteErrorTranslator {

    private final ObjectMapper mapper;

    public RemoteErrorTranslator(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public RuntimeException translate(String service, ClientHttpResponse response) {
        ApiError error;
        try {
            error = mapper.readValue(response.getBody(), ApiError.class);
        } catch (IOException | RuntimeException e) {
            return new UpstreamUnavailableException(service, e);
        }
        return switch (error.code()) {
            case DELIVERY_DELAYED -> new DeliveryDelayedException(error.message());
            case NOT_FOUND -> new NotFoundException(error.message());
            case BUSINESS_RULE_VIOLATION, VALIDATION_FAILED -> new BusinessRuleException(error.message());
            case UPSTREAM_UNAVAILABLE, INTERNAL_ERROR ->
                    new UpstreamUnavailableException(service, new IllegalStateException(error.message()));
        };
    }
}
