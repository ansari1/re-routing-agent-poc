package com.commerce.agent.routing;

import java.util.Comparator;

import com.commerce.common.contract.InventoryContracts.WarehouseView;

/** Deterministic fallback: the highest-priority warehouse that can fulfil the order. */
public class PriorityRouteAdvisor implements RouteAdvisor {

    @Override
    public WarehouseView choose(RouteContext context) {
        return context.candidates().stream()
                .min(Comparator.comparingInt(WarehouseView::priority).thenComparing(WarehouseView::id))
                .orElseThrow(() -> new IllegalArgumentException("No candidate warehouses"));
    }

    @Override
    public String name() {
        return "priority-rule";
    }
}
