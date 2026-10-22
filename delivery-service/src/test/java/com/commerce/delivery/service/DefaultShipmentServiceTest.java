package com.commerce.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commerce.common.contract.DeliveryContracts.ShipmentRequest;
import com.commerce.common.contract.DeliveryContracts.WeatherStatus;
import com.commerce.common.error.DeliveryDelayedException;
import com.commerce.common.error.NotFoundException;
import com.commerce.delivery.config.WeatherProperties;
import com.commerce.delivery.domain.Shipment;
import com.commerce.delivery.domain.ShipmentRepository;

@ExtendWith(MockitoExtension.class)
class DefaultShipmentServiceTest {

    @Mock
    ShipmentRepository repository;
    @Mock
    TrackingCodeGenerator trackingCodes;

    InMemoryWeatherService weather;
    DefaultShipmentService service;

    @BeforeEach
    void setUp() {
        weather = new InMemoryWeatherService(new WeatherProperties(false, Set.of()));
        service = new DefaultShipmentService(repository, weather, trackingCodes);
    }

    @Test
    void booksShipmentInClearWeather() {
        when(repository.findByOrderIdAndWarehouseId(1L, 2L)).thenReturn(Optional.empty());
        when(trackingCodes.next()).thenReturn("TRK-1");
        when(repository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        var view = service.book(new ShipmentRequest(1L, 2L, "NORTH", "Leeds"));

        assertThat(view.trackingCode()).isEqualTo("TRK-1");
    }

    @Test
    void severeWeatherInOriginRegionDelaysDelivery() {
        weather.update(new WeatherStatus(true, Set.of("SOUTH")));

        assertThatThrownBy(() -> service.book(new ShipmentRequest(1L, 2L, "SOUTH", "Leeds")))
                .isInstanceOf(DeliveryDelayedException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void severeWeatherElsewhereDoesNotBlockOtherRegions() {
        weather.update(new WeatherStatus(true, Set.of("SOUTH")));
        when(repository.findByOrderIdAndWarehouseId(1L, 3L)).thenReturn(Optional.empty());
        when(trackingCodes.next()).thenReturn("TRK-2");
        when(repository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        var view = service.book(new ShipmentRequest(1L, 3L, "NORTH", "Leeds"));

        assertThat(view.warehouseRegion()).isEqualTo("NORTH");
    }

    @Test
    void repeatedBookingReturnsExistingShipment() {
        var existing = new Shipment(1L, 2L, "NORTH", "Leeds", "TRK-9");
        when(repository.findByOrderIdAndWarehouseId(1L, 2L)).thenReturn(Optional.of(existing));

        var view = service.book(new ShipmentRequest(1L, 2L, "NORTH", "Leeds"));

        assertThat(view.trackingCode()).isEqualTo("TRK-9");
        verify(repository, never()).save(any());
    }

    @Test
    void getUnknownShipmentThrowsNotFound() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(5L)).isInstanceOf(NotFoundException.class);
    }
}
