package com.commerce.inventory.service;

import com.commerce.common.contract.InventoryContracts.StockUpdateRequest;

public interface StockService {

    void setStock(Long warehouseId, StockUpdateRequest request);
}
