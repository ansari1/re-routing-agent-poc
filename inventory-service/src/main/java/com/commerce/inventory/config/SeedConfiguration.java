package com.commerce.inventory.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import com.commerce.inventory.domain.StockLevel;
import com.commerce.inventory.domain.StockLevelRepository;
import com.commerce.inventory.domain.Warehouse;
import com.commerce.inventory.domain.WarehouseRepository;

@Configuration
public class SeedConfiguration {

    @Bean
    ApplicationRunner seedWarehouses(SeedProperties seed, WarehouseRepository warehouses,
                                     StockLevelRepository stock, TransactionTemplate tx) {
        return args -> {
            if (!seed.enabled() || warehouses.count() > 0) {
                return;
            }
            tx.executeWithoutResult(status -> seed.warehouses().forEach(w -> {
                var saved = warehouses.save(new Warehouse(w.code(), w.name(), w.region(), w.priority()));
                w.stock().forEach((sku, qty) -> stock.save(new StockLevel(saved.getId(), sku, qty)));
            }));
        };
    }
}
