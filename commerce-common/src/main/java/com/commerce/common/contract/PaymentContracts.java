package com.commerce.common.contract;

import java.math.BigDecimal;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Payment service API contracts. */
public interface PaymentContracts {

    @Schema(description = "How the customer pays", example = "CARD")
    enum PaymentMethod { CARD, PAYPAL, WALLET }

    @Schema(description = "Authorisation result; DECLINED when the amount exceeds the configured maximum",
            example = "AUTHORIZED")
    enum PaymentStatus { AUTHORIZED, DECLINED }

    @Schema(description = "Request to pay for an order. Idempotent per orderId: repeating the same request returns the "
            + "existing payment, a different request for the same order is rejected with 409")
    record PaymentRequest(
            @Schema(description = "Order being paid", example = "1")
            @NotNull Long orderId,
            @Schema(description = "Amount to charge; at least 0.01", example = "39.98", minimum = "0.01")
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @Schema(description = "Payment method", example = "CARD")
            @NotNull PaymentMethod method) {
    }

    @Schema(description = "A payment and its authorisation result")
    record PaymentView(
            @Schema(description = "Payment id", example = "1") Long id,
            @Schema(description = "Order that was paid", example = "1") Long orderId,
            @Schema(description = "Amount charged", example = "39.98") BigDecimal amount,
            @Schema(description = "Payment method", example = "CARD") PaymentMethod method,
            @Schema(description = "Authorisation result", example = "AUTHORIZED") PaymentStatus status,
            @Schema(description = "Creation time (UTC)", example = "2026-10-13T05:00:00Z") Instant createdAt) {
    }
}
