package com.main.util;

public class OrderServiceConstants {
    public static final String ORDER_NOT_FOUND = "Order with orderId %s does not exist";
    public static final String ORDER_NOT_FOUND_LOG = "Order with orderId {} not found";
    public static final String USER_NOT_EXISTS = "User Not Exists";
    public static final String USER_NOT_EXISTS_LOG = "User does not exist!";
    public static final String PRODUCT_NOT_FOUND = "Product not exist";
    public static final String PRODUCT_NOT_FOUND_IN_RESPONSE = "Product not found in response";
    public static final String PRODUCT_STOCK_INSUFFICIENT = "Product '%s' has only %d items left in stock, but %d were requested.";
    public static final String NO_ORDERS_FOUND_FOR_USER = "No orders found for userId: %s";
    public static final String ORDER_ALREADY_DELIVERED = "Order has already been delivered and cannot be canceled.";
    public static final String ORDER_ALREADY_CANCELLED = "Order is already canceled.";
    public static final String ORDER_CANCELLATION_INITIATED = "Initiating order cancellation for orderId: {}";
    public static final String STOCK_ROLLBACK_PROCESSING = "Processing stock rollback for orderId: {}";
    public static final String ORDER_STATUS_UPDATED = "Updating order status to CANCELED for orderId: {}";
    public static final String ORDER_SUCCESSFULLY_CANCELLED = "Order successfully canceled: {}";
}
