package com.commerce.inventory.domain;

import com.commerce.common.contract.InventoryContracts.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ReservationLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.RESERVED;

    protected ReservationLine() {
    }

    public ReservationLine(Long orderId, Long warehouseId, String sku, int quantity) {
        this.orderId = orderId;
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getWarehouseId() { return warehouseId; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }

    public void release() { this.status = ReservationStatus.RELEASED; }
}
