package com.commerce.order.service;

import org.springframework.stereotype.Component;

import com.commerce.common.contract.OrderContracts.OrderItem;
import com.commerce.common.contract.OrderContracts.OrderView;
import com.commerce.order.domain.OrderEntity;
import com.commerce.order.domain.OrderLine;

@Component
public class OrderMapper {

    public OrderLine toLine(OrderItem item) {
        return new OrderLine(item.sku(), item.quantity(), item.unitPrice());
    }

    public OrderView toView(OrderEntity order) {
        var items = order.getLines().stream()
                .map(l -> new OrderItem(l.sku(), l.quantity(), l.unitPrice()))
                .toList();
        return new OrderView(order.getId(), order.getCustomerId(), order.getDestination(), order.getStatus(),
                order.getWarehouseId(), order.getTotal(), items, order.getCreatedAt());
    }
}
