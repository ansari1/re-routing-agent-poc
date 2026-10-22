package com.commerce.inventory.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.commerce.common.contract.InventoryContracts.AvailabilityRequest;
import com.commerce.common.contract.InventoryContracts.WarehouseView;
import com.commerce.common.paging.PageResponse;

public interface AvailabilityService {

    PageResponse<WarehouseView> listWarehouses(Pageable pageable);

    /** Warehouses that can fulfil every line, best priority first. */
    List<WarehouseView> findAvailable(AvailabilityRequest request);
}
