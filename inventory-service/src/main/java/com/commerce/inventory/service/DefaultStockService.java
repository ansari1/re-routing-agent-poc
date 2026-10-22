package com.commerce.inventory.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce.common.contract.InventoryContracts.StockUpdateRequest;
import com.commerce.common.error.NotFoundException;
import com.commerce.inventory.domain.StockLevel;
import com.commerce.inventory.domain.StockLevelRepository;
import com.commerce.inventory.domain.WarehouseRepository;

@Service
@Transactional
public class DefaultStockService implements StockService {

    private final WarehouseRepository warehouses;
    private final StockLevelRepository stock;

    public DefaultStockService(WarehouseRepository warehouses, StockLevelRepository stock) {
        this.warehouses = warehouses;
        this.stock = stock;
    }

    @Override
    @CacheEvict(cacheNames = InventoryCaches.AVAILABILITY, allEntries = true)
    public void setStock(Long warehouseId, StockUpdateRequest request) {
        if (!warehouses.existsById(warehouseId)) {
            throw new NotFoundException("Warehouse", warehouseId);
        }
        var level = stock.findByWarehouseIdAndSku(warehouseId, request.sku())
                .orElseGet(() -> new StockLevel(warehouseId, request.sku(), 0));
        level.setQuantity(request.quantity());
        stock.save(level);
    }
}
