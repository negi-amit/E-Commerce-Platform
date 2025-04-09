package com.main.service;

import com.main.dtos.OrderRequest;
import com.main.dtos.OrderResponse;
import com.main.util.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest orderRequest);
    OrderResponse getOrderDetails(String id);
    List<OrderResponse> getAllOrderDetailsByUser(String userId);
    OrderResponse updateOrderDetails(String id, OrderRequest orderRequest);
    String cancelOrder(String id);
    void updateOrderStatus(String id, OrderStatus Status);
}
