package com.commerce.agent.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Base URLs and timeouts of the downstream services. */
@ConfigurationProperties(prefix = "app.services")
public record ServicesProperties(
        String orderUrl,
        String inventoryUrl,
        String paymentUrl,
        String crmUrl,
        String deliveryUrl,
        Duration connectTimeout,
        Duration readTimeout) {

    public ServicesProperties {
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(2) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(5) : readTimeout;
    }
}
