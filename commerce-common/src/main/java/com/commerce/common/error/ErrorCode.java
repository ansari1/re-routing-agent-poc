package com.commerce.common.error;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/** Stable machine-readable error codes shared by every service and client. */
@Schema(description = "VALIDATION_FAILED (400), NOT_FOUND (404), BUSINESS_RULE_VIOLATION (409), "
        + "DELIVERY_DELAYED (503), UPSTREAM_UNAVAILABLE (503), INTERNAL_ERROR (500)")
public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    BUSINESS_RULE_VIOLATION(HttpStatus.CONFLICT),
    DELIVERY_DELAYED(HttpStatus.SERVICE_UNAVAILABLE),
    UPSTREAM_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
