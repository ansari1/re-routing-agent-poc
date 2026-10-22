package com.commerce.payment.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.commerce.common.contract.PaymentContracts.PaymentRequest;
import com.commerce.common.contract.PaymentContracts.PaymentView;
import com.commerce.common.error.ApiError;
import com.commerce.common.openapi.ApiExamples.Errors;
import com.commerce.common.openapi.ApiExamples.Payments;
import com.commerce.common.paging.PageRequestFactory;
import com.commerce.common.paging.PageResponse;
import com.commerce.payment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payments")
@Validated
@Tag(name = "Payments", description = "Idempotent payment authorisation")
public class PaymentController {

    private final PaymentService service;
    private final PageRequestFactory pages;

    public PaymentController(PaymentService service, PageRequestFactory pages) {
        this.service = service;
        this.pages = pages;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Pay for an order (idempotent per order id)",
            description = "Authorises the payment with the simulated provider. Amounts above the configured maximum "
                    + "(PAYMENT_MAX_AMOUNT, default 10000) are DECLINED; a decline is still a 201 with `status: DECLINED`. "
                    + "Repeating an identical request returns the existing payment.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentRequest.class),
                            examples = {
                                    @ExampleObject(name = "Card payment", value = Payments.PAY_BY_CARD),
                                    @ExampleObject(name = "Amount above the limit", value = Payments.PAY_ABOVE_LIMIT)})))
    @ApiResponse(responseCode = "201", description = "Payment recorded (AUTHORIZED or DECLINED)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentView.class),
                    examples = {
                            @ExampleObject(name = "Authorized", value = Payments.AUTHORIZED),
                            @ExampleObject(name = "Declined", value = Payments.DECLINED)}))
    @ApiResponse(responseCode = "409", description = "The order already has a payment with different details",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Payment mismatch", value = Errors.PAYMENT_MISMATCH)))
    public PaymentView pay(@Valid @RequestBody PaymentRequest request) {
        return service.pay(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a payment", description = "Served from cache when available.")
    @ApiResponse(responseCode = "200", description = "The payment",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentView.class),
                    examples = @ExampleObject(name = "Authorized", value = Payments.AUTHORIZED)))
    @ApiResponse(responseCode = "404", description = "No payment with this id",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Not found", value = Errors.PAYMENT_NOT_FOUND)))
    public PaymentView get(@Parameter(description = "Payment id", example = "1") @PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List payments, paginated", description = "Sorted by id ascending.")
    @ApiResponse(responseCode = "200", description = "One page of payments")
    public PageResponse<PaymentView> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        return service.list(pages.create(page, size));
    }
}
