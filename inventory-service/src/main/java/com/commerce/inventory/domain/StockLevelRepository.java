package com.commerce.inventory.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {

    List<StockLevel> findByWarehouseIdInAndSkuIn(Collection<Long> warehouseIds, Collection<String> skus);

    Optional<StockLevel> findByWarehouseIdAndSku(Long warehouseId, String sku);
}
