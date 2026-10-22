package com.commerce.common.error;

/** Delivery cannot leave the origin warehouse (e.g. severe weather); the agent may re-route. */
public final class DeliveryDelayedException extends DomainException {

    public DeliveryDelayedException(String message) {
        super(ErrorCode.DELIVERY_DELAYED, message);
    }

    public DeliveryDelayedException(String message, Throwable cause) {
        super(ErrorCode.DELIVERY_DELAYED, message, cause);
    }
}
