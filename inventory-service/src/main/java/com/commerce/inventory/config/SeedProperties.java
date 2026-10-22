package com.commerce.inventory.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Demo data loaded at start-up; configured in application.yml rather than coded. */
@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(Boolean enabled, List<WarehouseSeed> warehouses) {

    public SeedProperties {
        enabled = enabled == null || enabled;
        warehouses = warehouses == null ? List.of() : List.copyOf(warehouses);
    }

    public record WarehouseSeed(String code, String name, String region, int priority, Map<String, Integer> stock) {

        public WarehouseSeed {
            stock = stock == null ? Map.of() : Map.copyOf(stock);
        }
    }
}
