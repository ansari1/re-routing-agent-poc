package com.commerce.delivery.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce.common.contract.DeliveryContracts.ShipmentRequest;
import com.commerce.common.contract.DeliveryContracts.ShipmentView;
import com.commerce.common.error.DeliveryDelayedException;
import com.commerce.common.error.NotFoundException;
import com.commerce.common.paging.PageResponse;
import com.commerce.delivery.domain.Shipment;
import com.commerce.delivery.domain.ShipmentRepository;

@Service
@Transactional
public class DefaultShipmentService implements ShipmentService {

    private final ShipmentRepository repository;
    private final WeatherService weather;
    private final TrackingCodeGenerator trackingCodes;

    public DefaultShipmentService(ShipmentRepository repository, WeatherService weather,
                                  TrackingCodeGenerator trackingCodes) {
        this.repository = repository;
        this.weather = weather;
        this.trackingCodes = trackingCodes;
    }

    @Override
    public ShipmentView book(ShipmentRequest request) {
        if (weather.current().blocks(request.warehouseRegion())) {
            throw new DeliveryDelayedException(
                    "Severe weather in %s: shipments cannot leave warehouse %d"
                            .formatted(request.warehouseRegion(), request.warehouseId()));
        }
        var shipment = repository.findByOrderIdAndWarehouseId(request.orderId(), request.warehouseId())
                .orElseGet(() -> repository.save(new Shipment(request.orderId(), request.warehouseId(),
                        request.warehouseRegion(), request.destination(), trackingCodes.next())));
        return toView(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "shipments", key = "#id")
    public ShipmentView get(Long id) {
        return repository.findById(id).map(DefaultShipmentService::toView)
                .orElseThrow(() -> new NotFoundException("Shipment", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShipmentView> list(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable), DefaultShipmentService::toView);
    }

    private static ShipmentView toView(Shipment s) {
        return new ShipmentView(s.getId(), s.getOrderId(), s.getWarehouseId(), s.getWarehouseRegion(),
                s.getDestination(), s.getStatus(), s.getTrackingCode(), s.getCreatedAt());
    }
}
