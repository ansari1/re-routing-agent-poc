package com.commerce.common.error;

public final class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}
