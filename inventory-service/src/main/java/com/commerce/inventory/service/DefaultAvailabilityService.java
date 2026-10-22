package com.commerce.inventory.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce.common.contract.InventoryContracts.AvailabilityRequest;
import com.commerce.common.contract.InventoryContracts.StockLine;
import com.commerce.common.contract.InventoryContracts.WarehouseView;
import com.commerce.common.paging.PageResponse;
import com.commerce.inventory.domain.StockLevel;
import com.commerce.inventory.domain.StockLevelRepository;
import com.commerce.inventory.domain.Warehouse;
import com.commerce.inventory.domain.WarehouseRepository;

@Service
@Transactional(readOnly = true)
public class DefaultAvailabilityService implements AvailabilityService {

    private static final Sort BY_PRIORITY = Sort.by("priority", "id");

    private final WarehouseRepository warehouses;
    private final StockLevelRepository stock;
    private final WarehouseMapper mapper;

    public DefaultAvailabilityService(WarehouseRepository warehouses, StockLevelRepository stock,
                                      WarehouseMapper mapper) {
        this.warehouses = warehouses;
        this.stock = stock;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<WarehouseView> listWarehouses(Pageable pageable) {
        return PageResponse.of(warehouses.findAll(pageable), mapper::toView);
    }

    @Override
    @Cacheable(cacheNames = InventoryCaches.AVAILABILITY, key = "#request")
    public List<WarehouseView> findAvailable(AvailabilityRequest request) {
        var candidates = request.excludedRegions().isEmpty()
                ? warehouses.findAll(BY_PRIORITY)
                : warehouses.findByRegionNotIn(request.excludedRegions(), BY_PRIORITY);
        if (candidates.isEmpty()) {
            return List.of();
        }

        var skus = request.items().stream().map(StockLine::sku).distinct().toList();
        var candidateIds = candidates.stream().map(Warehouse::getId).toList();
        Map<Long, Map<String, StockLevel>> stockByWarehouse = stock
                .findByWarehouseIdInAndSkuIn(candidateIds, skus).stream()
                .collect(Collectors.groupingBy(StockLevel::getWarehouseId,
                        Collectors.toMap(StockLevel::getSku, Function.identity())));

        return candidates.stream()
                .filter(w -> canFulfil(stockByWarehouse.getOrDefault(w.getId(), Map.of()), request.items()))
                .sorted(Comparator.comparingInt(Warehouse::getPriority).thenComparing(Warehouse::getId))
                .map(mapper::toView)
                .toList();
    }

    private static boolean canFulfil(Map<String, StockLevel> levels, List<StockLine> items) {
        return items.stream().allMatch(line -> {
            var level = levels.get(line.sku());
            return level != null && level.covers(line.quantity());
        });
    }
}
