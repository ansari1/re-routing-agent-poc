package com.commerce.payment.service;

import org.springframework.stereotype.Component;

import com.commerce.common.contract.PaymentContracts.PaymentRequest;
import com.commerce.common.contract.PaymentContracts.PaymentStatus;
import com.commerce.payment.config.PaymentProperties;

/** Offline stand-in for a payment provider: declines amounts above the configured limit. */
@Component
public class SimulatedPaymentProcessor implements PaymentProcessor {

    private final PaymentProperties properties;

    public SimulatedPaymentProcessor(PaymentProperties properties) {
        this.properties = properties;
    }

    @Override
    public PaymentStatus authorize(PaymentRequest request) {
        return request.amount().compareTo(properties.maxAmount()) > 0
                ? PaymentStatus.DECLINED
                : PaymentStatus.AUTHORIZED;
    }
}
