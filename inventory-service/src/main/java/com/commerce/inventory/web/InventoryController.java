package com.commerce.inventory.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.commerce.common.contract.InventoryContracts.AvailabilityRequest;
import com.commerce.common.contract.InventoryContracts.ReservationRequest;
import com.commerce.common.contract.InventoryContracts.ReservationView;
import com.commerce.common.contract.InventoryContracts.StockUpdateRequest;
import com.commerce.common.contract.InventoryContracts.WarehouseView;
import com.commerce.common.error.ApiError;
import com.commerce.common.openapi.ApiExamples.Errors;
import com.commerce.common.openapi.ApiExamples.Inventory;
import com.commerce.common.paging.PageRequestFactory;
import com.commerce.common.paging.PageResponse;
import com.commerce.inventory.service.AvailabilityService;
import com.commerce.inventory.service.ReservationService;
import com.commerce.inventory.service.StockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/inventory")
@Validated
@Tag(name = "Inventory", description = "Warehouses, stock, availability and reservations")
public class InventoryController {

    private final AvailabilityService availability;
    private final ReservationService reservations;
    private final StockService stock;
    private final PageRequestFactory pages;

    public InventoryController(AvailabilityService availability, ReservationService reservations,
                               StockService stock, PageRequestFactory pages) {
        this.availability = availability;
        this.reservations = reservations;
        this.stock = stock;
        this.pages = pages;
    }

    @GetMapping("/warehouses")
    @Operation(summary = "List warehouses, paginated",
            description = "Sorted by id ascending. Seed data: LON (SOUTH_ENGLAND), MAN (NORTH_ENGLAND), BHX (MIDLANDS).")
    @ApiResponse(responseCode = "200", description = "One page of warehouses")
    public PageResponse<WarehouseView> warehouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        return availability.listWarehouses(pages.create(page, size));
    }

    @PostMapping("/availability")
    @Operation(summary = "Find warehouses that can fulfil a basket, optionally excluding regions (cached)",
            description = "Returns every warehouse holding enough stock of all requested lines, ordered by priority. "
                    + "An empty list means no warehouse can fulfil the basket. The cache is cleared whenever stock "
                    + "is set, reserved or released.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AvailabilityRequest.class),
                            examples = {
                                    @ExampleObject(name = "Any region", value = Inventory.AVAILABILITY_BASKET),
                                    @ExampleObject(name = "Excluding a region",
                                            value = Inventory.AVAILABILITY_EXCLUDING_REGION)})))
    @ApiResponse(responseCode = "200", description = "Warehouses able to fulfil the basket, best first",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = WarehouseView.class)),
                    examples = {
                            @ExampleObject(name = "Two warehouses", value = Inventory.AVAILABLE_WAREHOUSES),
                            @ExampleObject(name = "None available", value = Inventory.NO_WAREHOUSES)}))
    public List<WarehouseView> available(@Valid @RequestBody AvailabilityRequest request) {
        return availability.findAvailable(request);
    }

    @PutMapping("/warehouses/{warehouseId}/stock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Set the stock level of a SKU in a warehouse",
            description = "Sets the absolute quantity; the SKU is created at that warehouse if it was not stocked.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockUpdateRequest.class),
                            examples = @ExampleObject(name = "Restock", value = Inventory.SET_STOCK))))
    @ApiResponse(responseCode = "204", description = "Stock level updated; no body")
    @ApiResponse(responseCode = "404", description = "No warehouse with this id",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Not found", value = Errors.WAREHOUSE_NOT_FOUND)))
    public void setStock(@Parameter(description = "Warehouse id", example = "1") @PathVariable Long warehouseId,
                         @Valid @RequestBody StockUpdateRequest request) {
        stock.setStock(warehouseId, request);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reserve stock for an order at a warehouse",
            description = "Deducts the lines from the warehouse's available stock. Each order can hold one active reservation.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservationRequest.class),
                            examples = @ExampleObject(name = "Reserve two units", value = Inventory.RESERVE))))
    @ApiResponse(responseCode = "201", description = "Stock reserved",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationView.class),
                    examples = @ExampleObject(name = "Reserved", value = Inventory.RESERVED)))
    @ApiResponse(responseCode = "409", description = "The order already has a reservation, or the warehouse lacks stock",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = {
                            @ExampleObject(name = "Already reserved", value = Errors.RESERVATION_EXISTS),
                            @ExampleObject(name = "Insufficient stock", value = Errors.INSUFFICIENT_STOCK)}))
    public ReservationView reserve(@Valid @RequestBody ReservationRequest request) {
        return reservations.reserve(request);
    }

    @DeleteMapping("/reservations/{orderId}")
    @Operation(summary = "Release the active reservation of an order",
            description = "Returns the reserved quantities to the warehouse.")
    @ApiResponse(responseCode = "200", description = "Reservation released",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationView.class),
                    examples = @ExampleObject(name = "Released", value = Inventory.RELEASED)))
    @ApiResponse(responseCode = "404", description = "The order has no active reservation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Not found", value = Errors.RESERVATION_NOT_FOUND)))
    public ReservationView release(@Parameter(description = "Order whose reservation is released", example = "1")
                                   @PathVariable Long orderId) {
        return reservations.release(orderId);
    }
}
