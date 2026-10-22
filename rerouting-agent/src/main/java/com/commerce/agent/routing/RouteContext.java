package com.commerce.agent.routing;

import java.util.List;

import com.commerce.common.contract.InventoryContracts.WarehouseView;
import com.commerce.common.contract.OrderContracts.OrderView;

/** Everything an advisor needs to pick a replacement warehouse. */
public record RouteContext(OrderView order, WarehouseView blocked, String reason, List<WarehouseView> candidates) {

    public RouteContext {
        candidates = List.copyOf(candidates);
    }
}
