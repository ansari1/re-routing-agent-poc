package com.commerce.payment.service;

import com.commerce.common.contract.PaymentContracts.PaymentRequest;
import com.commerce.common.contract.PaymentContracts.PaymentStatus;

/** Strategy for talking to a payment provider; swap the implementation for a real PSP. */
public interface PaymentProcessor {

    PaymentStatus authorize(PaymentRequest request);
}
