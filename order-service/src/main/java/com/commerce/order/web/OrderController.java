package com.commerce.order.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.commerce.common.contract.OrderContracts.CreateOrderRequest;
import com.commerce.common.contract.OrderContracts.OrderView;
import com.commerce.common.contract.OrderContracts.UpdateOrderStatusRequest;
import com.commerce.common.error.ApiError;
import com.commerce.common.openapi.ApiExamples.Errors;
import com.commerce.common.openapi.ApiExamples.Orders;
import com.commerce.common.paging.PageRequestFactory;
import com.commerce.common.paging.PageResponse;
import com.commerce.order.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Tag(name = "Orders", description = "Create and track customer orders")
public class OrderController {

    private final OrderService service;
    private final PageRequestFactory pages;

    public OrderController(OrderService service, PageRequestFactory pages) {
        this.service = service;
        this.pages = pages;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an order",
            description = "Creates an order in status CREATED and computes `total` as the sum of quantity x unitPrice. "
                    + "Stock is not reserved here; use the checkout agent or the inventory service for that.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreateOrderRequest.class),
                            examples = {
                                    @ExampleObject(name = "Single item", value = Orders.CREATE_SINGLE_ITEM),
                                    @ExampleObject(name = "Multiple items", value = Orders.CREATE_MULTIPLE_ITEMS)})))
    @ApiResponse(responseCode = "201", description = "Order created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderView.class),
                    examples = @ExampleObject(name = "Created order", value = Orders.CREATED)))
    public OrderView create(@Valid @RequestBody CreateOrderRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id", description = "Served from cache when available.")
    @ApiResponse(responseCode = "200", description = "The order",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderView.class),
                    examples = @ExampleObject(name = "Paid order", value = Orders.PAID)))
    @ApiResponse(responseCode = "404", description = "No order with this id",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Not found", value = Errors.ORDER_NOT_FOUND)))
    public OrderView get(@Parameter(description = "Order id", example = "1") @PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List orders, paginated", description = "Sorted by id ascending.")
    @ApiResponse(responseCode = "200", description = "One page of orders")
    public PageResponse<OrderView> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        return service.list(pages.create(page, size));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change order status (validated against allowed transitions)",
            description = "Allowed transitions: CREATED -> PAID | CANCELLED; PAID -> SHIPPED | DELIVERY_DELAYED | CANCELLED; "
                    + "DELIVERY_DELAYED -> REROUTED | SHIPPED | CANCELLED; REROUTED -> SHIPPED | DELIVERY_DELAYED. "
                    + "SHIPPED and CANCELLED are final. `warehouseId` is optional and only updated when supplied.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UpdateOrderStatusRequest.class),
                            examples = {
                                    @ExampleObject(name = "Mark as paid", value = Orders.MARK_PAID),
                                    @ExampleObject(name = "Cancel", value = Orders.CANCEL)})))
    @ApiResponse(responseCode = "200", description = "Order after the change",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderView.class),
                    examples = {
                            @ExampleObject(name = "Paid", value = Orders.PAID),
                            @ExampleObject(name = "Cancelled", value = Orders.CANCELLED)}))
    @ApiResponse(responseCode = "404", description = "No order with this id",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Not found", value = Errors.ORDER_NOT_FOUND)))
    @ApiResponse(responseCode = "409", description = "The status change is not an allowed transition",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Illegal transition", value = Errors.ILLEGAL_TRANSITION)))
    public OrderView updateStatus(@Parameter(description = "Order id", example = "1") @PathVariable Long id,
                                  @Valid @RequestBody UpdateOrderStatusRequest request) {
        return service.updateStatus(id, request);
    }
}
