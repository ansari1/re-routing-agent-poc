package com.commerce.agent.workflow;

import com.commerce.common.contract.DeliveryContracts.ShipmentView;
import com.commerce.common.contract.InventoryContracts.WarehouseView;

/** A booked shipment and the warehouse it ships from. */
public record Dispatch(WarehouseView warehouse, ShipmentView shipment, boolean rerouted) {
}
