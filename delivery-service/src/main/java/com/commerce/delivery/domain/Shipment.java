package com.commerce.delivery.domain;

import java.time.Instant;

import com.commerce.common.contract.DeliveryContracts.ShipmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

@Entity
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private String warehouseRegion;

    @Column(nullable = false)
    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.SCHEDULED;

    @Column(nullable = false, unique = true)
    private String trackingCode;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Shipment() {
    }

    public Shipment(Long orderId, Long warehouseId, String warehouseRegion, String destination, String trackingCode) {
        this.orderId = orderId;
        this.warehouseId = warehouseId;
        this.warehouseRegion = warehouseRegion;
        this.destination = destination;
        this.trackingCode = trackingCode;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getWarehouseId() { return warehouseId; }
    public String getWarehouseRegion() { return warehouseRegion; }
    public String getDestination() { return destination; }
    public ShipmentStatus getStatus() { return status; }
    public String getTrackingCode() { return trackingCode; }
    public Instant getCreatedAt() { return createdAt; }
}
