package com.commerce.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"warehouseId", "sku"}))
public class StockLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    protected StockLevel() {
    }

    public StockLevel(Long warehouseId, String sku, int quantity) {
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Long getWarehouseId() { return warehouseId; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean covers(int requested) {
        return quantity >= requested;
    }
}
