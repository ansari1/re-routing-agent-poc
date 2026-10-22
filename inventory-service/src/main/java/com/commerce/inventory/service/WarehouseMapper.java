package com.commerce.inventory.service;

import org.springframework.stereotype.Component;

import com.commerce.common.contract.InventoryContracts.WarehouseView;
import com.commerce.inventory.domain.Warehouse;

@Component
public class WarehouseMapper {

    public WarehouseView toView(Warehouse warehouse) {
        return new WarehouseView(warehouse.getId(), warehouse.getCode(), warehouse.getName(),
                warehouse.getRegion(), warehouse.getPriority());
    }
}
