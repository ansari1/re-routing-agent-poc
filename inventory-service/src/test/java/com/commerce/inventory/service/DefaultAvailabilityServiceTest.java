package com.commerce.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.commerce.common.contract.InventoryContracts.AvailabilityRequest;
import com.commerce.common.contract.InventoryContracts.StockLine;
import com.commerce.inventory.domain.StockLevel;
import com.commerce.inventory.domain.StockLevelRepository;
import com.commerce.inventory.domain.Warehouse;
import com.commerce.inventory.domain.WarehouseRepository;

@ExtendWith(MockitoExtension.class)
class DefaultAvailabilityServiceTest {

    @Mock
    WarehouseRepository warehouses;
    @Mock
    StockLevelRepository stock;

    DefaultAvailabilityService service;

    @BeforeEach
    void setUp() {
        service = new DefaultAvailabilityService(warehouses, stock, new WarehouseMapper());
    }

    @Test
    void returnsOnlyWarehousesWithEnoughStockOrderedByPriority() {
        var south = warehouse(1L, "LON", "SOUTH", 1);
        var north = warehouse(2L, "MAN", "NORTH", 2);
        var mids = warehouse(3L, "BHX", "MIDS", 3);
        when(warehouses.findAll(Sort.by("priority", "id"))).thenReturn(List.of(south, north, mids));
        when(stock.findByWarehouseIdInAndSkuIn(anyCollection(), anyCollection())).thenReturn(List.of(
                new StockLevel(1L, "A", 1),
                new StockLevel(2L, "A", 10),
                new StockLevel(3L, "A", 5)));

        var result = service.findAvailable(new AvailabilityRequest(List.of(new StockLine("A", 5)), null));

        assertThat(result).extracting("code").containsExactly("MAN", "BHX");
    }

    @Test
    void excludedRegionsAreNotQueried() {
        var north = warehouse(2L, "MAN", "NORTH", 2);
        when(warehouses.findByRegionNotIn(eq(Set.of("SOUTH")), eq(Sort.by("priority", "id"))))
                .thenReturn(List.of(north));
        when(stock.findByWarehouseIdInAndSkuIn(anyCollection(), anyCollection()))
                .thenReturn(List.of(new StockLevel(2L, "A", 10)));

        var result = service.findAvailable(new AvailabilityRequest(List.of(new StockLine("A", 1)), Set.of("SOUTH")));

        assertThat(result).extracting("code").containsExactly("MAN");
    }

    @Test
    void returnsEmptyWhenNoWarehouseRemains() {
        when(warehouses.findAll(Sort.by("priority", "id"))).thenReturn(List.of());

        assertThat(service.findAvailable(new AvailabilityRequest(List.of(new StockLine("A", 1)), Set.of()))).isEmpty();
    }

    private static Warehouse warehouse(Long id, String code, String region, int priority) {
        var w = new Warehouse(code, code, region, priority);
        ReflectionTestUtils.setField(w, "id", id);
        return w;
    }
}
