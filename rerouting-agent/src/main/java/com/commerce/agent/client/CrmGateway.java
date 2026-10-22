package com.commerce.agent.client;

import com.commerce.common.contract.CrmContracts.NotificationRequest;

public interface CrmGateway {

    void notifyCustomer(NotificationRequest request);
}
