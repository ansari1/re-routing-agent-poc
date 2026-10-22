package com.commerce.agent.web;

/** Swagger UI payload examples for the checkout endpoint, one per outcome. */
final class CheckoutExamples {

    private CheckoutExamples() {
    }

    static final String SINGLE_ITEM_REQUEST = """
            {"customerId": 1, "destination": "12 High St, Leeds", "paymentMethod": "CARD",
             "items": [{"sku": "SKU-100", "quantity": 2, "unitPrice": 19.99}]}""";

    static final String MULTIPLE_ITEMS_REQUEST = """
            {"customerId": 2, "destination": "5 Deansgate, Manchester", "paymentMethod": "PAYPAL",
             "items": [{"sku": "SKU-100", "quantity": 1, "unitPrice": 19.99},
                       {"sku": "SKU-200", "quantity": 3, "unitPrice": 7.50}]}""";

    static final String COMPLETED = """
            {"order": {"id": 1, "customerId": 1, "destination": "12 High St, Leeds", "status": "SHIPPED",
                       "warehouseId": 1, "total": 39.98,
                       "items": [{"sku": "SKU-100", "quantity": 2, "unitPrice": 19.99}],
                       "createdAt": "2026-10-13T05:00:00Z"},
             "outcome": "COMPLETED",
             "fulfilledFrom": {"id": 1, "code": "LON", "name": "London Central", "region": "SOUTH_ENGLAND", "priority": 1},
             "trackingCode": "TRK-3F9A1C2B",
             "timeline": ["Order 1 created", "Stock reserved at LON", "Payment authorised",
                          "Shipment booked from LON, tracking TRK-3F9A1C2B"]}""";

    static final String REROUTED = """
            {"order": {"id": 2, "customerId": 1, "destination": "12 High St, Leeds", "status": "SHIPPED",
                       "warehouseId": 2, "total": 39.98,
                       "items": [{"sku": "SKU-100", "quantity": 2, "unitPrice": 19.99}],
                       "createdAt": "2026-10-13T05:00:00Z"},
             "outcome": "REROUTED",
             "fulfilledFrom": {"id": 2, "code": "MAN", "name": "Manchester Hub", "region": "NORTH_ENGLAND", "priority": 2},
             "trackingCode": "TRK-7B20D4E1",
             "timeline": ["Order 2 created", "Stock reserved at LON", "Payment authorised",
                          "Delivery from LON delayed: Severe weather in SOUTH_ENGLAND: shipments cannot leave warehouse 1",
                          "Agent (priority-rule) selected warehouse MAN in NORTH_ENGLAND",
                          "Shipment booked from MAN, tracking TRK-7B20D4E1"]}""";

    static final String DELIVERY_DELAYED = """
            {"order": {"id": 3, "customerId": 1, "destination": "12 High St, Leeds", "status": "DELIVERY_DELAYED",
                       "warehouseId": 1, "total": 39.98,
                       "items": [{"sku": "SKU-100", "quantity": 2, "unitPrice": 19.99}],
                       "createdAt": "2026-10-13T05:00:00Z"},
             "outcome": "DELIVERY_DELAYED",
             "fulfilledFrom": {"id": 1, "code": "LON", "name": "London Central", "region": "SOUTH_ENGLAND", "priority": 1},
             "trackingCode": null,
             "timeline": ["Order 3 created", "Stock reserved at LON", "Payment authorised",
                          "Delivery from LON delayed: Severe weather in SOUTH_ENGLAND: shipments cannot leave warehouse 1",
                          "No alternative warehouse can fulfil the order"]}""";

    static final String PAYMENT_DECLINED = """
            {"order": {"id": 4, "customerId": 1, "destination": "12 High St, Leeds", "status": "CANCELLED",
                       "warehouseId": null, "total": 15000.00,
                       "items": [{"sku": "SKU-100", "quantity": 1, "unitPrice": 15000.00}],
                       "createdAt": "2026-10-13T05:00:00Z"},
             "outcome": "PAYMENT_DECLINED",
             "fulfilledFrom": null,
             "trackingCode": null,
             "timeline": ["Order 4 created", "Stock reserved at LON", "Order cancelled: payment was declined"]}""";

    static final String OUT_OF_STOCK = """
            {"order": {"id": 5, "customerId": 1, "destination": "12 High St, Leeds", "status": "CANCELLED",
                       "warehouseId": null, "total": 9995.00,
                       "items": [{"sku": "SKU-100", "quantity": 500, "unitPrice": 19.99}],
                       "createdAt": "2026-10-13T05:00:00Z"},
             "outcome": "OUT_OF_STOCK",
             "fulfilledFrom": null,
             "trackingCode": null,
             "timeline": ["Order 5 created", "Order cancelled: no warehouse has the requested stock"]}""";
}
