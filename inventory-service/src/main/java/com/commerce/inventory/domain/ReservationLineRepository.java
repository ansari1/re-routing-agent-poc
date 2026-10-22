package com.commerce.inventory.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerce.common.contract.InventoryContracts.ReservationStatus;

public interface ReservationLineRepository extends JpaRepository<ReservationLine, Long> {

    List<ReservationLine> findByOrderIdAndStatus(Long orderId, ReservationStatus status);
}
