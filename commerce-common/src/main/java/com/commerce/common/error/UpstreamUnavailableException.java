package com.commerce.common.error;

/** A downstream dependency failed, timed out or has an open circuit. */
public final class UpstreamUnavailableException extends DomainException {

    public UpstreamUnavailableException(String service, Throwable cause) {
        super(ErrorCode.UPSTREAM_UNAVAILABLE, "Service '%s' is currently unavailable".formatted(service), cause);
    }
}
