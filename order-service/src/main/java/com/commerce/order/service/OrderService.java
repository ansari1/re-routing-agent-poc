package com.commerce.order.service;

import org.springframework.data.domain.Pageable;

import com.commerce.common.contract.OrderContracts.CreateOrderRequest;
import com.commerce.common.contract.OrderContracts.OrderView;
import com.commerce.common.contract.OrderContracts.UpdateOrderStatusRequest;
import com.commerce.common.paging.PageResponse;

public interface OrderService {

    OrderView create(CreateOrderRequest request);

    OrderView get(Long id);

    PageResponse<OrderView> list(Pageable pageable);

    OrderView updateStatus(Long id, UpdateOrderStatusRequest request);
}
