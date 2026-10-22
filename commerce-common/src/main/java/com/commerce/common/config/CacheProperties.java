package com.commerce.common.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache")
public record CacheProperties(Duration ttl, Long maximumSize) {

    public CacheProperties {
        ttl = ttl == null ? Duration.ofMinutes(5) : ttl;
        maximumSize = maximumSize == null ? 1_000L : maximumSize;
    }
}
