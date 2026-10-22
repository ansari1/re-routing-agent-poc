package com.commerce.agent.workflow;

import com.commerce.agent.api.CheckoutRequest;
import com.commerce.agent.api.CheckoutResult;

public interface CheckoutAgent {

    CheckoutResult checkout(CheckoutRequest request);
}
