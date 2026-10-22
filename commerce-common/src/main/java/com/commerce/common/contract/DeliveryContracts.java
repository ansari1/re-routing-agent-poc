package com.commerce.common.contract;

import java.time.Instant;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Delivery service API contracts. */
public interface DeliveryContracts {

    @Schema(description = "Shipment state", example = "SCHEDULED")
    enum ShipmentStatus { SCHEDULED }

    @Schema(description = "Request to book a shipment; rejected with 503 DELIVERY_DELAYED when the origin region "
            + "has severe weather. Idempotent per order and warehouse")
    record ShipmentRequest(
            @Schema(description = "Order to ship", example = "1")
            @NotNull Long orderId,
            @Schema(description = "Warehouse the shipment leaves from", example = "2")
            @NotNull Long warehouseId,
            @Schema(description = "Region of that warehouse, checked against weather restrictions; must not be blank",
                    example = "NORTH_ENGLAND", minLength = 1)
            @NotBlank String warehouseRegion,
            @Schema(description = "Delivery address; must not be blank", example = "12 High St, Leeds", minLength = 1)
            @NotBlank String destination) {
    }

    @Schema(description = "A booked shipment")
    record ShipmentView(
            @Schema(description = "Shipment id", example = "1") Long id,
            @Schema(description = "Order being shipped", example = "1") Long orderId,
            @Schema(description = "Origin warehouse", example = "2") Long warehouseId,
            @Schema(description = "Region of the origin warehouse", example = "NORTH_ENGLAND") String warehouseRegion,
            @Schema(description = "Delivery address", example = "12 High St, Leeds") String destination,
            @Schema(description = "Shipment state", example = "SCHEDULED") ShipmentStatus status,
            @Schema(description = "Carrier tracking code", example = "TRK-7B20D4E1") String trackingCode,
            @Schema(description = "Creation time (UTC)", example = "2026-10-13T05:00:00Z") Instant createdAt) {
    }

    @Schema(description = "Weather restrictions that block shipments from the affected regions")
    record WeatherStatus(
            @Schema(description = "True when severe weather is in effect", example = "true") boolean severe,
            @ArraySchema(arraySchema = @Schema(description = "Regions where shipments cannot leave while severe is true",
                    example = "[\"SOUTH_ENGLAND\"]"), schema = @Schema(example = "SOUTH_ENGLAND"))
            Set<String> affectedRegions) {

        public WeatherStatus {
            affectedRegions = affectedRegions == null ? Set.of() : Set.copyOf(affectedRegions);
        }

        public boolean blocks(String region) {
            return severe && affectedRegions.contains(region);
        }
    }
}
