package com.commerce.payment.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment")
public record PaymentProperties(BigDecimal maxAmount) {

    public PaymentProperties {
        maxAmount = maxAmount == null ? new BigDecimal("10000") : maxAmount;
    }
}
