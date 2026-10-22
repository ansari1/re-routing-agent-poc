package com.commerce.crm.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

@Entity
public class CustomerActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    private Long orderId;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected CustomerActivity() {
    }

    public CustomerActivity(Long customerId, Long orderId, String message) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.message = message;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Long getOrderId() { return orderId; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
