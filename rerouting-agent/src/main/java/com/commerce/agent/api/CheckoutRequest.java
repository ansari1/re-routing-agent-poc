package com.commerce.agent.api;

import java.util.List;

import com.commerce.common.contract.OrderContracts.OrderItem;
import com.commerce.common.contract.PaymentContracts.PaymentMethod;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Everything the agent needs to place and fulfil an order")
public record CheckoutRequest(
        @Schema(description = "Id of the ordering customer (see the CRM service)", example = "1")
        @NotNull Long customerId,
        @Schema(description = "Delivery address; must not be blank", example = "12 High St, Leeds", minLength = 1)
        @NotBlank String destination,
        @Schema(description = "Payment method", example = "CARD")
        @NotNull PaymentMethod paymentMethod,
        @ArraySchema(arraySchema = @Schema(description = "Items to order; at least one"), minItems = 1)
        @NotEmpty @Valid List<OrderItem> items) {
}
