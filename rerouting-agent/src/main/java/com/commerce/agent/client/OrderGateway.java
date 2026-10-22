package com.commerce.agent.client;

import com.commerce.common.contract.OrderContracts.CreateOrderRequest;
import com.commerce.common.contract.OrderContracts.OrderView;
import com.commerce.common.contract.OrderContracts.UpdateOrderStatusRequest;

public interface OrderGateway {

    OrderView create(CreateOrderRequest request);

    OrderView updateStatus(Long orderId, UpdateOrderStatusRequest request);
}
