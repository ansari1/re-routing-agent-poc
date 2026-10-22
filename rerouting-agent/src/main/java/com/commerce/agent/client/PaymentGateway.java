package com.commerce.agent.client;

import com.commerce.common.contract.PaymentContracts.PaymentRequest;
import com.commerce.common.contract.PaymentContracts.PaymentView;

public interface PaymentGateway {

    PaymentView pay(PaymentRequest request);
}
