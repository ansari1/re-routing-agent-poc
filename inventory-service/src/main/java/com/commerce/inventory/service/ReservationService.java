package com.commerce.inventory.service;

import com.commerce.common.contract.InventoryContracts.ReservationRequest;
import com.commerce.common.contract.InventoryContracts.ReservationView;

public interface ReservationService {

    ReservationView reserve(ReservationRequest request);

    ReservationView release(Long orderId);
}
