package com.commerce.inventory.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commerce.common.contract.InventoryContracts.ReservationRequest;
import com.commerce.common.contract.InventoryContracts.ReservationStatus;
import com.commerce.common.contract.InventoryContracts.ReservationView;
import com.commerce.common.error.BusinessRuleException;
import com.commerce.common.error.NotFoundException;
import com.commerce.inventory.domain.ReservationLine;
import com.commerce.inventory.domain.ReservationLineRepository;
import com.commerce.inventory.domain.StockLevel;
import com.commerce.inventory.domain.StockLevelRepository;

@Service
@Transactional
@CacheEvict(cacheNames = InventoryCaches.AVAILABILITY, allEntries = true)
public class DefaultReservationService implements ReservationService {

    private final StockLevelRepository stock;
    private final ReservationLineRepository reservations;

    public DefaultReservationService(StockLevelRepository stock, ReservationLineRepository reservations) {
        this.stock = stock;
        this.reservations = reservations;
    }

    @Override
    public ReservationView reserve(ReservationRequest request) {
        if (!reservations.findByOrderIdAndStatus(request.orderId(), ReservationStatus.RESERVED).isEmpty()) {
            throw new BusinessRuleException("Order %d already has an active reservation".formatted(request.orderId()));
        }

        var lines = request.items().stream().map(item -> {
            var level = stock.findByWarehouseIdAndSku(request.warehouseId(), item.sku())
                    .filter(l -> l.covers(item.quantity()))
                    .orElseThrow(() -> new BusinessRuleException("Warehouse %d lacks %d x %s"
                            .formatted(request.warehouseId(), item.quantity(), item.sku())));
            level.setQuantity(level.getQuantity() - item.quantity());
            return new Reserved(level, new ReservationLine(request.orderId(), request.warehouseId(),
                    item.sku(), item.quantity()));
        }).toList();

        stock.saveAll(lines.stream().map(Reserved::level).toList());
        reservations.saveAll(lines.stream().map(Reserved::line).toList());
        return new ReservationView(request.orderId(), request.warehouseId(), ReservationStatus.RESERVED);
    }

    @Override
    public ReservationView release(Long orderId) {
        List<ReservationLine> active = reservations.findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED);
        if (active.isEmpty()) {
            throw new NotFoundException("Active reservation for order", orderId);
        }
        active.forEach(line -> {
            stock.findByWarehouseIdAndSku(line.getWarehouseId(), line.getSku())
                    .ifPresent(level -> level.setQuantity(level.getQuantity() + line.getQuantity()));
            line.release();
        });
        reservations.saveAll(active);
        return new ReservationView(orderId, active.get(0).getWarehouseId(), ReservationStatus.RELEASED);
    }

    private record Reserved(StockLevel level, ReservationLine line) {
    }
}
