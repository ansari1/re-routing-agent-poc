package com.commerce.agent.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commerce.agent.api.CheckoutRequest;
import com.commerce.agent.api.CheckoutResult;
import com.commerce.agent.workflow.CheckoutAgent;
import com.commerce.common.error.ApiError;
import com.commerce.common.openapi.ApiExamples.Errors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/agent")
@Tag(name = "Checkout agent", description = "Autonomous checkout with delivery re-routing")
public class CheckoutController {

    private final CheckoutAgent agent;

    public CheckoutController(CheckoutAgent agent) {
        this.agent = agent;
    }

    @PostMapping("/checkouts")
    @Operation(summary = "Place an order; the agent re-routes to another warehouse if delivery is delayed",
            description = "Runs the whole flow: create the order, reserve stock at the best warehouse, take payment, "
                    + "book the shipment and notify the customer. When the origin warehouse cannot ship (severe weather), "
                    + "the agent picks another warehouse able to fulfil the order and re-books. The HTTP status mirrors "
                    + "`outcome`: 201 COMPLETED or REROUTED, 202 DELIVERY_DELAYED, 402 PAYMENT_DECLINED, 409 OUT_OF_STOCK. "
                    + "Every outcome body has the same shape; `timeline` records each step taken.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CheckoutRequest.class),
                            examples = {
                                    @ExampleObject(name = "Single item, card", value = CheckoutExamples.SINGLE_ITEM_REQUEST),
                                    @ExampleObject(name = "Multiple items, PayPal",
                                            value = CheckoutExamples.MULTIPLE_ITEMS_REQUEST)})))
    @ApiResponse(responseCode = "201", description = "Order shipped, from the primary warehouse (COMPLETED) or an alternative (REROUTED)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CheckoutResult.class),
                    examples = {
                            @ExampleObject(name = "Completed (normal weather)", value = CheckoutExamples.COMPLETED),
                            @ExampleObject(name = "Rerouted (severe weather in SOUTH_ENGLAND)",
                                    value = CheckoutExamples.REROUTED)}))
    @ApiResponse(responseCode = "202", description = "Paid, but no warehouse can ship now; the order waits as DELIVERY_DELAYED",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CheckoutResult.class),
                    examples = @ExampleObject(name = "Delivery delayed", value = CheckoutExamples.DELIVERY_DELAYED)))
    @ApiResponse(responseCode = "402", description = "Payment declined; stock released and the order cancelled",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CheckoutResult.class),
                    examples = @ExampleObject(name = "Payment declined", value = CheckoutExamples.PAYMENT_DECLINED)))
    @ApiResponse(responseCode = "409", description = "No warehouse has the requested stock; the order is cancelled",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CheckoutResult.class),
                    examples = @ExampleObject(name = "Out of stock", value = CheckoutExamples.OUT_OF_STOCK)))
    @ApiResponse(responseCode = "503", description = "A downstream service or its circuit breaker is unavailable",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Upstream unavailable", value = Errors.UPSTREAM_UNAVAILABLE)))
    public ResponseEntity<CheckoutResult> checkout(@Valid @RequestBody CheckoutRequest request) {
        var result = agent.checkout(request);
        return ResponseEntity.status(result.outcome().httpStatus()).body(result);
    }
}
