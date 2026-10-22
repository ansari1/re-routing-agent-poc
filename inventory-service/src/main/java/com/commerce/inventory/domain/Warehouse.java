package com.commerce.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String region;

    /** Lower value means preferred. */
    @Column(nullable = false)
    private int priority;

    protected Warehouse() {
    }

    public Warehouse(String code, String name, String region, int priority) {
        this.code = code;
        this.name = name;
        this.region = region;
        this.priority = priority;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getRegion() { return region; }
    public int getPriority() { return priority; }
}
