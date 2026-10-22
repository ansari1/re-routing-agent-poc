package com.commerce.common.error;

/** Base of all expected business failures; the code drives the HTTP status. */
public abstract sealed class DomainException extends RuntimeException
        permits NotFoundException, BusinessRuleException, UpstreamUnavailableException, DeliveryDelayedException {

    private final ErrorCode errorCode;

    protected DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
