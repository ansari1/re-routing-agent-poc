package com.commerce.delivery.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Initial weather state; can be changed at runtime through the weather endpoint. */
@ConfigurationProperties(prefix = "app.delivery.weather")
public record WeatherProperties(Boolean severe, Set<String> affectedRegions) {

    public WeatherProperties {
        severe = severe != null && severe;
        affectedRegions = affectedRegions == null ? Set.of() : Set.copyOf(affectedRegions);
    }
}
