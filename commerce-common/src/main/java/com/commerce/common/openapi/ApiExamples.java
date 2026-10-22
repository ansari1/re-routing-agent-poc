package com.commerce.common.openapi;

/** JSON payload examples shown in Swagger UI; constants so annotations across services reuse one definition. */
public final class ApiExamples {

    private ApiExamples() {
    }

    public interface Errors {
        String NOT_FOUND_PREFIX = "{\"timestamp\": \"2026-10-13T05:00:00Z\", \"status\": 404, \"code\": \"NOT_FOUND\", \"message\": \"";
        String CONFLICT_PREFIX = "{\"timestamp\": \"2026-10-13T05:00:00Z\", \"status\": 409, "
                + "\"code\": \"BUSINESS_RULE_VIOLATION\", \"message\": \"";
        String SUFFIX = "\", \"details\": []}";

        String VALIDATION_FAILED = """
                {"timestamp": "2026-10-13T05:00:00Z", "status": 400, "code": "VALIDATION_FAILED",
                 "message": "Request validation failed",
                 "details": ["items: must not be empty", "destination: must not be blank"]}""";
        String MALFORMED_REQUEST = """
                {"timestamp": "2026-10-13T05:00:00Z", "status": 400, "code": "VALIDATION_FAILED",
                 "message": "Malformed request", "details": []}""";
        String INTERNAL_ERROR = """
                {"timestamp": "2026-10-13T05:00:00Z", "status": 500, "code": "INTERNAL_ERROR",
                 "message": "Unexpected error", "details": []}""";
        String UPSTREAM_UNAVAILABLE = """
                {"timestamp": "2026-10-13T05:00:00Z", "status": 503, "code": "UPSTREAM_UNAVAILABLE",
                 "message": "inventory-service is unavailable", "details": []}""";
        String ORDER_NOT_FOUND = NOT_FOUND_PREFIX + "Order 99 was not found" + SUFFIX;
        String CUSTOMER_NOT_FOUND = NOT_FOUND_PREFIX + "Customer 99 was not found" + SUFFIX;
        String PAYMENT_NOT_FOUND = NOT_FOUND_PREFIX + "Payment 99 was not found" + SUFFIX;
        String SHIPMENT_NOT_FOUND = NOT_FOUND_PREFIX + "Shipment 99 was not found" + SUFFIX;
        String WAREHOUSE_NOT_FOUND = NOT_FOUND_PREFIX + "Warehouse 99 was not found" + SUFFIX;
        String RESERVATION_NOT_FOUND = NOT_FOUND_PREFIX + "Active reservation for order 99 was not found" + SUFFIX;
        String ILLEGAL_TRANSITION = CONFLICT_PREFIX + "Order 1 cannot move from CREATED to SHIPPED" + SUFFIX;
        String EMAIL_TAKEN = CONFLICT_PREFIX + "A customer with email asha@example.com already exists" + SUFFIX;
        String PAYMENT_MISMATCH = CONFLICT_PREFIX + "Order 1 already has a different payment" + SUFFIX;
        String RESERVATION_EXISTS = CONFLICT_PREFIX + "Order 1 already has an active reservation" + SUFFIX;
        String INSUFFICIENT_STOCK = CONFLICT_PREFIX + "Warehouse 1 lacks 500 x SKU-100" + SUFFIX;
        String DELIVERY_DELAYED = """
                {"timestamp": "2026-10-13T05:00:00Z", "status": 503, "code": "DELIVERY_DELAYED",
                 "message": "Severe weather in SOUTH_ENGLAND: shipments cannot leave warehouse 1", "details": []}""";
    }

    public interface Orders {
        String CREATE_SINGLE_ITEM = """
                {"customerId": 1, "destination": "12 High St, Leeds",
                 "items": [{"sku": "SKU-100", "quantity": 2, "unitPrice": 19.99}]}""";
        String CREATE_MULTIPLE_ITEMS = """
                {"customerId": 2, "destination": "5 Deansgate, Manchester",
                 "items": [{"sku": "SKU-100", "quantity": 1, "unitPrice": 19.99},
                           {"sku": "SKU-200", "quantity": 3, "unitPrice": 7.50}]}""";
        String CREATED = """
                {"id": 1, "customerId": 1, "destination": "12 High St, Leeds", "status": "CREATED",
                 "warehouseId": null, "total": 39.98,
                 "items": [{"sku": "SKU-100", "quantity": 2, "unitPrice": 19.99}],
                 "createdAt": "2026-10-13T05:00:00Z"}""";
        String MARK_PAID = "{\"status\": \"PAID\", \"warehouseId\": 1}";
        String CANCEL = "{\"status\": \"CANCELLED\"}";
        String PAID = """
                {"id": 1, "customerId": 1, "destination": "12 High St, Leeds", "status": "PAID",
                 "warehouseId": 1, "total": 39.98,
                 "items": [{"sku": "SKU-100", "quantity": 2, "unitPrice": 19.99}],
                 "createdAt": "2026-10-13T05:00:00Z"}""";
        String CANCELLED = """
                {"id": 1, "customerId": 1, "destination": "12 High St, Leeds", "status": "CANCELLED",
                 "warehouseId": null, "total": 39.98,
                 "items": [{"sku": "SKU-100", "quantity": 2, "unitPrice": 19.99}],
                 "createdAt": "2026-10-13T05:00:00Z"}""";
    }

    public interface Inventory {
        String AVAILABILITY_BASKET = "{\"items\": [{\"sku\": \"SKU-100\", \"quantity\": 2}]}";
        String AVAILABILITY_EXCLUDING_REGION = """
                {"items": [{"sku": "SKU-100", "quantity": 2}], "excludedRegions": ["SOUTH_ENGLAND"]}""";
        String AVAILABLE_WAREHOUSES = """
                [{"id": 2, "code": "MAN", "name": "Manchester Hub", "region": "NORTH_ENGLAND", "priority": 2},
                 {"id": 3, "code": "BHX", "name": "Birmingham Depot", "region": "MIDLANDS", "priority": 3}]""";
        String NO_WAREHOUSES = "[]";
        String SET_STOCK = "{\"sku\": \"SKU-100\", \"quantity\": 100}";
        String RESERVE = """
                {"orderId": 1, "warehouseId": 1, "items": [{"sku": "SKU-100", "quantity": 2}]}""";
        String RESERVED = "{\"orderId\": 1, \"warehouseId\": 1, \"status\": \"RESERVED\"}";
        String RELEASED = "{\"orderId\": 1, \"warehouseId\": 1, \"status\": \"RELEASED\"}";
    }

    public interface Payments {
        String PAY_BY_CARD = "{\"orderId\": 1, \"amount\": 39.98, \"method\": \"CARD\"}";
        String PAY_ABOVE_LIMIT = "{\"orderId\": 2, \"amount\": 15000.00, \"method\": \"PAYPAL\"}";
        String AUTHORIZED = """
                {"id": 1, "orderId": 1, "amount": 39.98, "method": "CARD", "status": "AUTHORIZED",
                 "createdAt": "2026-10-13T05:00:00Z"}""";
        String DECLINED = """
                {"id": 2, "orderId": 2, "amount": 15000.00, "method": "PAYPAL", "status": "DECLINED",
                 "createdAt": "2026-10-13T05:00:00Z"}""";
    }

    public interface Crm {
        String CUSTOMER_REQUEST = "{\"name\": \"Asha Rao\", \"email\": \"asha@example.com\"}";
        String CUSTOMER = "{\"id\": 1, \"name\": \"Asha Rao\", \"email\": \"asha@example.com\"}";
        String NOTIFICATION_REQUEST = """
                {"customerId": 1, "orderId": 2,
                 "message": "Order 2 was delayed by SOUTH_ENGLAND, so it is now shipping from MAN."}""";
        String ACTIVITY = """
                {"id": 1, "customerId": 1, "orderId": 2,
                 "message": "Order 2 was delayed by SOUTH_ENGLAND, so it is now shipping from MAN.",
                 "createdAt": "2026-10-13T05:00:00Z"}""";
    }

    public interface Delivery {
        String BOOK = """
                {"orderId": 1, "warehouseId": 2, "warehouseRegion": "NORTH_ENGLAND", "destination": "12 High St, Leeds"}""";
        String SHIPMENT = """
                {"id": 1, "orderId": 1, "warehouseId": 2, "warehouseRegion": "NORTH_ENGLAND",
                 "destination": "12 High St, Leeds", "status": "SCHEDULED", "trackingCode": "TRK-7B20D4E1",
                 "createdAt": "2026-10-13T05:00:00Z"}""";
        String SEVERE_WEATHER = "{\"severe\": true, \"affectedRegions\": [\"SOUTH_ENGLAND\"]}";
        String CLEAR_WEATHER = "{\"severe\": false, \"affectedRegions\": []}";
    }
}
