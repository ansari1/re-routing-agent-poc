package com.commerce.delivery.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class TrackingCodeGenerator {

    public String next() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
