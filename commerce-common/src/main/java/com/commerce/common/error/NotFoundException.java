package com.commerce.common.error;

public final class NotFoundException extends DomainException {

    public NotFoundException(String resource, Object id) {
        super(ErrorCode.NOT_FOUND, "%s %s was not found".formatted(resource, id));
    }

    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
