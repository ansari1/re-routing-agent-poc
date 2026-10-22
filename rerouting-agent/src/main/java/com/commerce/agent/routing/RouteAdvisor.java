package com.commerce.agent.routing;

import com.commerce.common.contract.InventoryContracts.WarehouseView;

/** Strategy for choosing a replacement warehouse; the result is always one of the candidates. */
public interface RouteAdvisor {

    WarehouseView choose(RouteContext context);

    String name();
}
