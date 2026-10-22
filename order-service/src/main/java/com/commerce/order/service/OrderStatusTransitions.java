package com.commerce.order.service;

import java.util.Set;

import com.commerce.common.contract.OrderContracts.OrderStatus;

/** Encodes which status changes are legal, keeping that rule out of the service. */
public final class OrderStatusTransitions {

    private OrderStatusTransitions() {
    }

    public static Set<OrderStatus> allowedFrom(OrderStatus current) {
        return switch (current) {
            case CREATED -> Set.of(OrderStatus.PAID, OrderStatus.CANCELLED);
            case PAID -> Set.of(OrderStatus.SHIPPED, OrderStatus.DELIVERY_DELAYED, OrderStatus.CANCELLED);
            case DELIVERY_DELAYED -> Set.of(OrderStatus.REROUTED, OrderStatus.SHIPPED, OrderStatus.CANCELLED);
            case REROUTED -> Set.of(OrderStatus.SHIPPED, OrderStatus.DELIVERY_DELAYED);
            case SHIPPED, CANCELLED -> Set.of();
        };
    }

    public static boolean canMove(OrderStatus from, OrderStatus to) {
        return allowedFrom(from).contains(to);
    }
}
