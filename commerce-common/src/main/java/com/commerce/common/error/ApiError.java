package com.commerce.common.error;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Uniform error payload returned by every service. Clients should branch on `code`; "
        + "`message` is human readable and may change")
public record ApiError(
        @Schema(description = "When the error occurred (UTC)", example = "2026-10-13T05:00:00Z") Instant timestamp,
        @Schema(description = "HTTP status code, repeated from the response line", example = "404") int status,
        @Schema(description = "Stable machine-readable error code", example = "NOT_FOUND") ErrorCode code,
        @Schema(description = "Human readable summary", example = "Order 99 was not found") String message,
        @ArraySchema(arraySchema = @Schema(description = "Per-field validation messages as `field: message`; empty for other errors",
                example = "[\"destination: must not be blank\"]"), schema = @Schema(example = "destination: must not be blank"))
                List<String> details) {

    public static ApiError of(ErrorCode code, String message, List<String> details) {
        return new ApiError(Instant.now(), code.status().value(), code, message, details);
    }

    public static ApiError of(ErrorCode code, String message) {
        return of(code, message, List.of());
    }
}
