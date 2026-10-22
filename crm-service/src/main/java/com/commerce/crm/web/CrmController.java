package com.commerce.crm.web;

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

import com.commerce.common.contract.CrmContracts.ActivityView;
import com.commerce.common.contract.CrmContracts.CustomerRequest;
import com.commerce.common.contract.CrmContracts.CustomerView;
import com.commerce.common.contract.CrmContracts.NotificationRequest;
import com.commerce.common.error.ApiError;
import com.commerce.common.openapi.ApiExamples.Crm;
import com.commerce.common.openapi.ApiExamples.Errors;
import com.commerce.common.paging.PageRequestFactory;
import com.commerce.common.paging.PageResponse;
import com.commerce.crm.service.CrmService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/crm")
@Validated
@Tag(name = "CRM", description = "Customers and customer-facing notifications")
public class CrmController {

    private final CrmService service;
    private final PageRequestFactory pages;

    public CrmController(CrmService service, PageRequestFactory pages) {
        this.service = service;
        this.pages = pages;
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a customer", description = "The email address must be unique.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerRequest.class),
                            examples = @ExampleObject(name = "New customer", value = Crm.CUSTOMER_REQUEST))))
    @ApiResponse(responseCode = "201", description = "Customer registered",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerView.class),
                    examples = @ExampleObject(name = "Customer", value = Crm.CUSTOMER)))
    @ApiResponse(responseCode = "409", description = "A customer with this email already exists",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Duplicate email", value = Errors.EMAIL_TAKEN)))
    public CustomerView createCustomer(@Valid @RequestBody CustomerRequest request) {
        return service.createCustomer(request);
    }

    @GetMapping("/customers/{id}")
    @Operation(summary = "Get a customer", description = "Served from cache when available.")
    @ApiResponse(responseCode = "200", description = "The customer",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerView.class),
                    examples = @ExampleObject(name = "Customer", value = Crm.CUSTOMER)))
    @ApiResponse(responseCode = "404", description = "No customer with this id",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Not found", value = Errors.CUSTOMER_NOT_FOUND)))
    public CustomerView getCustomer(@Parameter(description = "Customer id", example = "1") @PathVariable Long id) {
        return service.getCustomer(id);
    }

    @GetMapping("/customers")
    @Operation(summary = "List customers, paginated", description = "Sorted by id ascending.")
    @ApiResponse(responseCode = "200", description = "One page of customers")
    public PageResponse<CustomerView> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        return service.listCustomers(pages.create(page, size));
    }

    @PostMapping("/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a notification sent to a customer",
            description = "Appends an entry to the customer's activity log.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificationRequest.class),
                            examples = @ExampleObject(name = "Delay notice", value = Crm.NOTIFICATION_REQUEST))))
    @ApiResponse(responseCode = "201", description = "Notification recorded",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ActivityView.class),
                    examples = @ExampleObject(name = "Activity", value = Crm.ACTIVITY)))
    @ApiResponse(responseCode = "404", description = "No customer with this id",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Not found", value = Errors.CUSTOMER_NOT_FOUND)))
    public ActivityView notifyCustomer(@Valid @RequestBody NotificationRequest request) {
        return service.notifyCustomer(request);
    }

    @GetMapping("/customers/{id}/activities")
    @Operation(summary = "List a customer's activity log, paginated", description = "Sorted by id ascending.")
    @ApiResponse(responseCode = "200", description = "One page of activity entries")
    @ApiResponse(responseCode = "404", description = "No customer with this id",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Not found", value = Errors.CUSTOMER_NOT_FOUND)))
    public PageResponse<ActivityView> activities(
            @Parameter(description = "Customer id", example = "1") @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        return service.listActivities(id, pages.create(page, size));
    }
}
