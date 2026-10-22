package com.commerce.agent.client;

import com.commerce.common.contract.DeliveryContracts.ShipmentRequest;
import com.commerce.common.contract.DeliveryContracts.ShipmentView;

public interface DeliveryGateway {

    /** @throws com.commerce.common.error.DeliveryDelayedException when the origin region cannot ship right now */
    ShipmentView book(ShipmentRequest request);
}
