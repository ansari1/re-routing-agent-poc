package com.commerce.common.contract;

import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Inventory service API contracts. */
public interface InventoryContracts {

    @Schema(description = "A SKU and the quantity needed")
    record StockLine(
            @Schema(description = "Stock keeping unit; must not be blank", example = "SKU-100", minLength = 1)
            @NotBlank String sku,
            @Schema(description = "Units needed; at least 1", example = "2", minimum = "1",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @Min(1) int quantity) {
    }

    @Schema(description = "A fulfilment warehouse")
    record WarehouseView(
            @Schema(description = "Warehouse id", example = "1") Long id,
            @Schema(description = "Short unique code", example = "LON") String code,
            @Schema(description = "Display name", example = "London Central") String name,
            @Schema(description = "Region served; matched against weather restrictions",
                    example = "SOUTH_ENGLAND") String region,
            @Schema(description = "Preference order, lowest first", example = "1") int priority) {
    }

    @Schema(description = "Request to set the stock level of one SKU in a warehouse")
    record StockUpdateRequest(
            @Schema(description = "Stock keeping unit; must not be blank", example = "SKU-100", minLength = 1)
            @NotBlank String sku,
            @Schema(description = "New absolute stock level; 0 or more", example = "100", minimum = "0",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @Min(0) int quantity) {
    }

    /** Warehouses able to fulfil every line, excluding the listed regions. Immutable, so it is a safe cache key. */
    @Schema(description = "Basket to check; warehouses able to fulfil every line are returned, ordered by priority")
    record AvailabilityRequest(
            @ArraySchema(arraySchema = @Schema(description = "Lines to fulfil; at least one"), minItems = 1)
            @NotEmpty @Valid List<StockLine> items,
            @ArraySchema(arraySchema = @Schema(description = "Regions to leave out, for example those hit by severe weather; optional",
                    example = "[\"SOUTH_ENGLAND\"]"), schema = @Schema(example = "SOUTH_ENGLAND"))
            Set<String> excludedRegions) {

        public AvailabilityRequest {
            items = items == null ? List.of() : List.copyOf(items);
            excludedRegions = excludedRegions == null ? Set.of() : Set.copyOf(excludedRegions);
        }
    }

    @Schema(description = "Request to reserve stock for an order at one warehouse")
    record ReservationRequest(
            @Schema(description = "Order the stock is reserved for; one active reservation per order", example = "1")
            @NotNull Long orderId,
            @Schema(description = "Warehouse holding the stock", example = "1")
            @NotNull Long warehouseId,
            @ArraySchema(arraySchema = @Schema(description = "Lines to reserve; at least one"), minItems = 1)
            @NotEmpty @Valid List<StockLine> items) {
    }

    @Schema(description = "Reservation state", example = "RESERVED")
    enum ReservationStatus { RESERVED, RELEASED }

    @Schema(description = "Outcome of a reservation or release")
    record ReservationView(
            @Schema(description = "Order the stock is reserved for", example = "1") Long orderId,
            @Schema(description = "Warehouse holding the stock", example = "1") Long warehouseId,
            @Schema(description = "Current state", example = "RESERVED") ReservationStatus status) {
    }
}
