package com.commerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.commerce.common.contract.OrderContracts.OrderStatus;

class OrderStatusTransitionsTest {

    @Test
    void terminalStatesHaveNoTransitions() {
        assertThat(OrderStatusTransitions.allowedFrom(OrderStatus.SHIPPED)).isEmpty();
        assertThat(OrderStatusTransitions.allowedFrom(OrderStatus.CANCELLED)).isEmpty();
    }

    @Test
    void delayedOrdersCanBeRerouted() {
        assertThat(OrderStatusTransitions.canMove(OrderStatus.DELIVERY_DELAYED, OrderStatus.REROUTED)).isTrue();
        assertThat(OrderStatusTransitions.canMove(OrderStatus.CREATED, OrderStatus.SHIPPED)).isFalse();
    }
}
