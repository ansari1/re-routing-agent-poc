package com.commerce.delivery.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByOrderIdAndWarehouseId(Long orderId, Long warehouseId);
}
