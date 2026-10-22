package com.commerce.delivery.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.commerce.common.contract.DeliveryContracts.ShipmentRequest;
import com.commerce.common.contract.DeliveryContracts.ShipmentView;
import com.commerce.common.contract.DeliveryContracts.WeatherStatus;
import com.commerce.common.error.ApiError;
import com.commerce.common.openapi.ApiExamples.Delivery;
import com.commerce.common.openapi.ApiExamples.Errors;
import com.commerce.common.paging.PageRequestFactory;
import com.commerce.common.paging.PageResponse;
import com.commerce.delivery.service.ShipmentService;
import com.commerce.delivery.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Delivery", description = "Shipment booking and weather conditions")
public class DeliveryController {

    private final ShipmentService shipments;
    private final WeatherService weather;
    private final PageRequestFactory pages;

    public DeliveryController(ShipmentService shipments, WeatherService weather, PageRequestFactory pages) {
        this.shipments = shipments;
        this.weather = weather;
        this.pages = pages;
    }

    @PostMapping("/shipments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Book a shipment; responds 503 DELIVERY_DELAYED when the origin region has severe weather",
            description = "Idempotent per order and warehouse: repeating the request returns the existing shipment. "
                    + "To see the 503 response, first mark the region as affected with `PUT /api/v1/delivery/weather`.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShipmentRequest.class),
                            examples = @ExampleObject(name = "Ship from Manchester", value = Delivery.BOOK))))
    @ApiResponse(responseCode = "201", description = "Shipment booked",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShipmentView.class),
                    examples = @ExampleObject(name = "Scheduled shipment", value = Delivery.SHIPMENT)))
    @ApiResponse(responseCode = "503", description = "Severe weather in the origin region; the shipment cannot leave",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Delivery delayed", value = Errors.DELIVERY_DELAYED)))
    public ShipmentView book(@Valid @RequestBody ShipmentRequest request) {
        return shipments.book(request);
    }

    @GetMapping("/shipments/{id}")
    @Operation(summary = "Get a shipment", description = "Served from cache when available.")
    @ApiResponse(responseCode = "200", description = "The shipment",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShipmentView.class),
                    examples = @ExampleObject(name = "Scheduled shipment", value = Delivery.SHIPMENT)))
    @ApiResponse(responseCode = "404", description = "No shipment with this id",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(name = "Not found", value = Errors.SHIPMENT_NOT_FOUND)))
    public ShipmentView get(@Parameter(description = "Shipment id", example = "1") @PathVariable Long id) {
        return shipments.get(id);
    }

    @GetMapping("/shipments")
    @Operation(summary = "List shipments, paginated", description = "Sorted by id ascending.")
    @ApiResponse(responseCode = "200", description = "One page of shipments")
    public PageResponse<ShipmentView> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        return shipments.list(pages.create(page, size));
    }

    @GetMapping("/delivery/weather")
    @Operation(summary = "Current weather restrictions",
            description = "Shipments cannot leave a region listed in `affectedRegions` while `severe` is true. "
                    + "The initial state comes from WEATHER_SEVERE and WEATHER_AFFECTED_REGIONS.")
    @ApiResponse(responseCode = "200", description = "Current restrictions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeatherStatus.class),
                    examples = {
                            @ExampleObject(name = "Severe weather", value = Delivery.SEVERE_WEATHER),
                            @ExampleObject(name = "Clear", value = Delivery.CLEAR_WEATHER)}))
    public WeatherStatus weather() {
        return weather.current();
    }

    @PutMapping("/delivery/weather")
    @Operation(summary = "Update weather restrictions (simulates a weather feed)",
            description = "Replaces the current restrictions; takes effect for the next shipment booking.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WeatherStatus.class),
                            examples = {
                                    @ExampleObject(name = "Block SOUTH_ENGLAND", value = Delivery.SEVERE_WEATHER),
                                    @ExampleObject(name = "Clear all restrictions", value = Delivery.CLEAR_WEATHER)})))
    @ApiResponse(responseCode = "200", description = "Restrictions now in effect",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeatherStatus.class),
                    examples = @ExampleObject(name = "Severe weather", value = Delivery.SEVERE_WEATHER)))
    public WeatherStatus updateWeather(@RequestBody WeatherStatus status) {
        return weather.update(status);
    }
}
