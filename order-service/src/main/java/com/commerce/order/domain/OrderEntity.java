package com.commerce.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.commerce.common.contract.OrderContracts.OrderStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    private Long warehouseId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderLine> lines = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected OrderEntity() {
    }

    public OrderEntity(Long customerId, String destination, BigDecimal total, List<OrderLine> lines) {
        this.customerId = customerId;
        this.destination = destination;
        this.total = total;
        this.lines = new ArrayList<>(lines);
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getDestination() { return destination; }
    public OrderStatus getStatus() { return status; }
    public Long getWarehouseId() { return warehouseId; }
    public BigDecimal getTotal() { return total; }
    public List<OrderLine> getLines() { return List.copyOf(lines); }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(OrderStatus status) { this.status = status; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
}
