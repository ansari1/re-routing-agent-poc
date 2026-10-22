package com.commerce.agent.api;

import java.util.List;

import com.commerce.common.contract.InventoryContracts.WarehouseView;
import com.commerce.common.contract.OrderContracts.OrderView;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "What the agent did for a checkout and how it ended")
public record CheckoutResult(
        @Schema(description = "The order in its final state") OrderView order,
        @Schema(description = "How the checkout ended; also determines the HTTP status") CheckoutOutcome outcome,
        @Schema(description = "Warehouse the order ships from; null when nothing could be shipped",
                nullable = true) WarehouseView fulfilledFrom,
        @Schema(description = "Carrier tracking code; null unless a shipment was booked",
                example = "TRK-3F9A1C2B", nullable = true) String trackingCode,
        @ArraySchema(arraySchema = @Schema(description = "Ordered log of the steps the agent took",
                example = "[\"Order 1 created\", \"Stock reserved at LON\", \"Payment authorised\", "
                        + "\"Shipment booked from LON, tracking TRK-3F9A1C2B\"]"),
                schema = @Schema(example = "Stock reserved at LON")) List<String> timeline) {
}
