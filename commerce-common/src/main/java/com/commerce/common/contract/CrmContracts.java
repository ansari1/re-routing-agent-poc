package com.commerce.common.contract;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** CRM service API contracts. */
public interface CrmContracts {

    @Schema(description = "Request to register a customer; the email must be unique")
    record CustomerRequest(
            @Schema(description = "Full name; must not be blank", example = "Asha Rao", minLength = 1)
            @NotBlank String name,
            @Schema(description = "Email address; must be well formed and not already registered",
                    example = "asha@example.com", format = "email", minLength = 1)
            @NotBlank @Email String email) {
    }

    @Schema(description = "A registered customer")
    record CustomerView(
            @Schema(description = "Customer id", example = "1") Long id,
            @Schema(description = "Full name", example = "Asha Rao") String name,
            @Schema(description = "Email address", example = "asha@example.com") String email) {
    }

    @Schema(description = "Request to record a notification sent to a customer")
    record NotificationRequest(
            @Schema(description = "Customer that was notified", example = "1")
            @NotNull Long customerId,
            @Schema(description = "Related order; optional", example = "2")
            Long orderId,
            @Schema(description = "Notification text; must not be blank", minLength = 1,
                    example = "Order 2 was delayed by SOUTH_ENGLAND, so it is now shipping from MAN.")
            @NotBlank String message) {
    }

    @Schema(description = "An entry in a customer's activity log")
    record ActivityView(
            @Schema(description = "Activity id", example = "1") Long id,
            @Schema(description = "Customer the entry belongs to", example = "1") Long customerId,
            @Schema(description = "Related order, if any", example = "2", nullable = true) Long orderId,
            @Schema(description = "Notification text",
                    example = "Order 2 was delayed by SOUTH_ENGLAND, so it is now shipping from MAN.") String message,
            @Schema(description = "Creation time (UTC)", example = "2026-10-13T05:00:00Z") Instant createdAt) {
    }
}
