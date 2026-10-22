package com.commerce.order.service;

import java.math.BigDecimal;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce.common.contract.OrderContracts.CreateOrderRequest;
import com.commerce.common.contract.OrderContracts.OrderItem;
import com.commerce.common.contract.OrderContracts.OrderView;
import com.commerce.common.contract.OrderContracts.UpdateOrderStatusRequest;
import com.commerce.common.error.BusinessRuleException;
import com.commerce.common.error.NotFoundException;
import com.commerce.common.paging.PageResponse;
import com.commerce.order.domain.OrderEntity;
import com.commerce.order.domain.OrderRepository;

@Service
@Transactional
public class DefaultOrderService implements OrderService {

    static final String CACHE = "orders";

    private final OrderRepository repository;
    private final OrderMapper mapper;

    public DefaultOrderService(OrderRepository repository, OrderMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public OrderView create(CreateOrderRequest request) {
        var total = request.items().stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var lines = request.items().stream().map(mapper::toLine).toList();
        var saved = repository.save(new OrderEntity(request.customerId(), request.destination(), total, lines));
        return mapper.toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE, key = "#id")
    public OrderView get(Long id) {
        return mapper.toView(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderView> list(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable), mapper::toView);
    }

    @Override
    @CacheEvict(cacheNames = CACHE, key = "#id")
    public OrderView updateStatus(Long id, UpdateOrderStatusRequest request) {
        var order = find(id);
        if (!OrderStatusTransitions.canMove(order.getStatus(), request.status())) {
            throw new BusinessRuleException("Order %d cannot move from %s to %s"
                    .formatted(id, order.getStatus(), request.status()));
        }
        order.setStatus(request.status());
        if (request.warehouseId() != null) {
            order.setWarehouseId(request.warehouseId());
        }
        return mapper.toView(repository.save(order));
    }

    private OrderEntity find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Order", id));
    }
}
