package com.commerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commerce.common.contract.OrderContracts.CreateOrderRequest;
import com.commerce.common.contract.OrderContracts.OrderItem;
import com.commerce.common.contract.OrderContracts.OrderStatus;
import com.commerce.common.contract.OrderContracts.UpdateOrderStatusRequest;
import com.commerce.common.error.BusinessRuleException;
import com.commerce.common.error.NotFoundException;
import com.commerce.order.domain.OrderEntity;
import com.commerce.order.domain.OrderLine;
import com.commerce.order.domain.OrderRepository;

@ExtendWith(MockitoExtension.class)
class DefaultOrderServiceTest {

    @Mock
    OrderRepository repository;

    DefaultOrderService service;

    @BeforeEach
    void setUp() {
        service = new DefaultOrderService(repository, new OrderMapper());
    }

    @Test
    void createComputesTotalFromLines() {
        when(repository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        var request = new CreateOrderRequest(1L, "Leeds", List.of(
                new OrderItem("A", 2, new BigDecimal("10.00")),
                new OrderItem("B", 1, new BigDecimal("5.50"))));

        var view = service.create(request);

        assertThat(view.total()).isEqualByComparingTo("25.50");
        assertThat(view.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(view.items()).hasSize(2);
    }

    @Test
    void getUnknownOrderThrowsNotFound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(9L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateStatusAppliesLegalTransitionAndWarehouse() {
        var order = existingOrder(OrderStatus.PAID);
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(order);

        var view = service.updateStatus(1L, new UpdateOrderStatusRequest(OrderStatus.SHIPPED, 7L));

        assertThat(view.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(view.warehouseId()).isEqualTo(7L);
    }

    @Test
    void updateStatusRejectsIllegalTransition() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingOrder(OrderStatus.SHIPPED)));

        assertThatThrownBy(() -> service.updateStatus(1L, new UpdateOrderStatusRequest(OrderStatus.CREATED, null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    private static OrderEntity existingOrder(OrderStatus status) {
        var order = new OrderEntity(1L, "Leeds", BigDecimal.TEN, List.of(new OrderLine("A", 1, BigDecimal.TEN)));
        order.setStatus(status);
        return order;
    }
}
