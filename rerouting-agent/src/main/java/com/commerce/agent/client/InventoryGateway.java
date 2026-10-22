package com.commerce.agent.client;

import java.util.List;

import com.commerce.common.contract.InventoryContracts.AvailabilityRequest;
import com.commerce.common.contract.InventoryContracts.ReservationRequest;
import com.commerce.common.contract.InventoryContracts.ReservationView;
import com.commerce.common.contract.InventoryContracts.WarehouseView;

public interface InventoryGateway {

    List<WarehouseView> findAvailable(AvailabilityRequest request);

    ReservationView reserve(ReservationRequest request);

    ReservationView release(Long orderId);
}
