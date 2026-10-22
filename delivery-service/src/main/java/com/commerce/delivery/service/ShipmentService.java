package com.commerce.delivery.service;

import org.springframework.data.domain.Pageable;

import com.commerce.common.contract.DeliveryContracts.ShipmentRequest;
import com.commerce.common.contract.DeliveryContracts.ShipmentView;
import com.commerce.common.paging.PageResponse;

public interface ShipmentService {

    /** @throws com.commerce.common.error.DeliveryDelayedException when the origin region is weather-blocked */
    ShipmentView book(ShipmentRequest request);

    ShipmentView get(Long id);

    PageResponse<ShipmentView> list(Pageable pageable);
}
