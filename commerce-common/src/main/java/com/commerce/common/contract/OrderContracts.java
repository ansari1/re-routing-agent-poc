package com.commerce.common.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Order service API contracts. */
public interface OrderContracts {

    @Schema(description = "Order lifecycle state. Allowed transitions: CREATED -> PAID | CANCELLED; "
            + "PAID -> SHIPPED | DELIVERY_DELAYED | CANCELLED; "
            + "DELIVERY_DELAYED -> REROUTED | SHIPPED | CANCELLED; REROUTED -> SHIPPED | DELIVERY_DELAYED; "
            + "SHIPPED and CANCELLED are final.", example = "PAID")
    enum OrderStatus { CREATED, PAID, SHIPPED, DELIVERY_DELAYED, REROUTED, CANCELLED }

    @Schema(description = "A line of an order: one SKU, its quantity and unit price")
    record OrderItem(
            @Schema(description = "Stock keeping unit; must not be blank", example = "SKU-100", minLength = 1)
            @NotBlank String sku,
            @Schema(description = "Units ordered; at least 1", example = "2", minimum = "1",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @Min(1) int quantity,
            @Schema(description = "Price of one unit; at least 0.01", example = "19.99", minimum = "0.01")
            @NotNull @DecimalMin("0.01") BigDecimal unitPrice) {

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }

        public InventoryContracts.StockLine toStockLine() {
            return new InventoryContracts.StockLine(sku, quantity);
        }
    }

    @Schema(description = "Request to create an order")
    record CreateOrderRequest(
            @Schema(description = "Id of the ordering customer (see the CRM service)", example = "1")
            @NotNull Long customerId,
            @Schema(description = "Delivery address; must not be blank", example = "12 High St, Leeds", minLength = 1)
            @NotBlank String destination,
            @ArraySchema(arraySchema = @Schema(description = "Items to order; at least one"), minItems = 1)
            @NotEmpty @Valid List<OrderItem> items) {
    }

    @Schema(description = "Request to change the status of an order; checked against the allowed transitions")
    record UpdateOrderStatusRequest(
            @Schema(description = "Target status", example = "PAID")
            @NotNull OrderStatus status,
            @Schema(description = "Warehouse fulfilling the order; optional, kept unchanged when omitted", example = "1")
            Long warehouseId) {
    }

    @Schema(description = "An order as stored by the order service")
    record OrderView(
            @Schema(description = "Order id", example = "1") Long id,
            @Schema(description = "Id of the ordering customer", example = "1") Long customerId,
            @Schema(description = "Delivery address", example = "12 High St, Leeds") String destination,
            @Schema(description = "Current status", example = "CREATED") OrderStatus status,
            @Schema(description = "Warehouse fulfilling the order; null until one is assigned",
                    example = "1", nullable = true) Long warehouseId,
            @Schema(description = "Sum of quantity x unitPrice over all items", example = "39.98") BigDecimal total,
            @Schema(description = "Ordered items") List<OrderItem> items,
            @Schema(description = "Creation time (UTC)", example = "2026-10-13T05:00:00Z") Instant createdAt) {
    }
}
