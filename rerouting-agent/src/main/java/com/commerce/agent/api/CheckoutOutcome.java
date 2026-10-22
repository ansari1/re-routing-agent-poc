package com.commerce.agent.api;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "COMPLETED (201): shipped from the primary warehouse. REROUTED (201): shipped from another warehouse. "
        + "DELIVERY_DELAYED (202): paid but no warehouse can ship yet. PAYMENT_DECLINED (402): order cancelled. "
        + "OUT_OF_STOCK (409): order cancelled.", example = "COMPLETED")
public enum CheckoutOutcome {
    COMPLETED(HttpStatus.CREATED),
    REROUTED(HttpStatus.CREATED),
    DELIVERY_DELAYED(HttpStatus.ACCEPTED),
    PAYMENT_DECLINED(HttpStatus.PAYMENT_REQUIRED),
    OUT_OF_STOCK(HttpStatus.CONFLICT);

    private final HttpStatus httpStatus;

    CheckoutOutcome(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
