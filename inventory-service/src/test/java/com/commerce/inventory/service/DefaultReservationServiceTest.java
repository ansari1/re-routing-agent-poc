package com.commerce.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commerce.common.contract.InventoryContracts.ReservationRequest;
import com.commerce.common.contract.InventoryContracts.ReservationStatus;
import com.commerce.common.contract.InventoryContracts.StockLine;
import com.commerce.common.error.BusinessRuleException;
import com.commerce.common.error.NotFoundException;
import com.commerce.inventory.domain.ReservationLine;
import com.commerce.inventory.domain.ReservationLineRepository;
import com.commerce.inventory.domain.StockLevel;
import com.commerce.inventory.domain.StockLevelRepository;

@ExtendWith(MockitoExtension.class)
class DefaultReservationServiceTest {

    @Mock
    StockLevelRepository stock;
    @Mock
    ReservationLineRepository reservations;

    DefaultReservationService service;

    @BeforeEach
    void setUp() {
        service = new DefaultReservationService(stock, reservations);
    }

    @Test
    void reserveDecrementsStock() {
        var level = new StockLevel(1L, "A", 10);
        when(reservations.findByOrderIdAndStatus(5L, ReservationStatus.RESERVED)).thenReturn(List.of());
        when(stock.findByWarehouseIdAndSku(1L, "A")).thenReturn(Optional.of(level));

        var view = service.reserve(new ReservationRequest(5L, 1L, List.of(new StockLine("A", 4))));

        assertThat(level.getQuantity()).isEqualTo(6);
        assertThat(view.status()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void reserveFailsWhenStockIsInsufficient() {
        when(reservations.findByOrderIdAndStatus(5L, ReservationStatus.RESERVED)).thenReturn(List.of());
        when(stock.findByWarehouseIdAndSku(1L, "A")).thenReturn(Optional.of(new StockLevel(1L, "A", 1)));

        assertThatThrownBy(() -> service.reserve(new ReservationRequest(5L, 1L, List.of(new StockLine("A", 4)))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void reserveRejectsDuplicateActiveReservation() {
        when(reservations.findByOrderIdAndStatus(5L, ReservationStatus.RESERVED))
                .thenReturn(List.of(new ReservationLine(5L, 1L, "A", 1)));

        assertThatThrownBy(() -> service.reserve(new ReservationRequest(5L, 1L, List.of(new StockLine("A", 1)))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void releaseRestoresStockAndMarksLinesReleased() {
        var line = new ReservationLine(5L, 1L, "A", 4);
        var level = new StockLevel(1L, "A", 6);
        when(reservations.findByOrderIdAndStatus(5L, ReservationStatus.RESERVED)).thenReturn(List.of(line));
        when(stock.findByWarehouseIdAndSku(1L, "A")).thenReturn(Optional.of(level));

        var view = service.release(5L);

        assertThat(level.getQuantity()).isEqualTo(10);
        assertThat(line.getStatus()).isEqualTo(ReservationStatus.RELEASED);
        assertThat(view.status()).isEqualTo(ReservationStatus.RELEASED);
    }

    @Test
    void releaseWithoutActiveReservationThrowsNotFound() {
        when(reservations.findByOrderIdAndStatus(9L, ReservationStatus.RESERVED)).thenReturn(List.of());

        assertThatThrownBy(() -> service.release(9L)).isInstanceOf(NotFoundException.class);
    }
}
