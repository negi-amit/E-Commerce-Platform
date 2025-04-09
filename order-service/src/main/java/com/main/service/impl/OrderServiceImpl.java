package com.main.service.impl;

import com.main.Exceptions.BusinessException;
import com.main.dtos.OrderItemDto;
import com.main.dtos.OrderRequest;
import com.main.dtos.OrderItemRequestDto;
import com.main.dtos.OrderResponse;
import com.main.dtos.PaymentRequest;
import com.main.dtos.PaymentResponse;
import com.main.dtos.ProductResponse;
import com.main.dtos.StockUpdateRequest;
import com.main.dtos.UserResponse;
import com.main.entity.Order;
import com.main.entity.OrderItem;
import com.main.feign.PaymentClient;
import com.main.feign.ProductClient;
import com.main.feign.UserClient;
import com.main.repository.OrderRepository;
import com.main.service.OrderService;
import com.main.util.OrderServiceConstants;
import com.main.util.OrderStatus;
import com.main.util.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;

    public OrderServiceImpl(OrderRepository orderRepository, ModelMapper modelMapper, UserClient userClient, ProductClient productClient, PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
        this.userClient = userClient;
        this.productClient = productClient;
        this.paymentClient = paymentClient;
    }

    @Override
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        /* Verify user existence */
        UserResponse user = userClient.getUserById(orderRequest.getUserId());
        if (user == null) {
            log.info(OrderServiceConstants.USER_NOT_EXISTS_LOG);
            throw new BusinessException(OrderServiceConstants.USER_NOT_EXISTS);
        }

        /* Retrieve product details */
        List<String> productIds = orderRequest.getItems().stream()
                .map(OrderItemRequestDto::getProductId)
                .distinct()
                .toList();
        ResponseEntity<List<ProductResponse>> response = productClient.getProductsByIds(productIds);
        List<ProductResponse> productList = response.getBody();
        if (productList == null || productList.isEmpty()) {
            throw new BusinessException(OrderServiceConstants.PRODUCT_NOT_FOUND);
        }

        /* Map products by ID for efficient lookup */
        Map<String, ProductResponse> productMap = productList.stream()
                .collect(Collectors.toMap(ProductResponse::getId, Function.identity()));

        /* Map OrderRequest DTO to Order entity */
        Order order = modelMapper.map(orderRequest, Order.class);

        /* Populate OrderItem details with trusted product data */
        for (OrderItem item : order.getItems()) {
            ProductResponse product = productMap.get(item.getProductId());
            if (product == null) {
                throw new BusinessException(String.format(OrderServiceConstants.PRODUCT_NOT_FOUND, item.getProductId()));
            }
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setTotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        /* Validate stock availability */
        validateStockAvailability(order.getItems(), productMap);

        /* Deduct stock */
        updateStock(order.getItems(), -1);

        /* Calculate total price directly */
        BigDecimal totalPrice = order.getItems().stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        orderRepository.save(order);

        // Process payment
        PaymentResponse paymentResponse = processPayment(order, orderRequest);

        if (PaymentStatus.SUCCESS.name().equals(paymentResponse.getStatus())) {
            order.setStatus(OrderStatus.PLACED);
        } else {
            order.setStatus(OrderStatus.FAILED);
            updateStock(order.getItems(), 1); // Restore stock on failure
        }

        orderRepository.save(order);

        // Map order items to OrderItemDto
        List<OrderItemDto> orderItemDtos = order.getItems().stream()
                .map(item -> {
                    OrderItemDto dto = modelMapper.map(item, OrderItemDto.class);
                    dto.setProductName(item.getProductName()); // Ensure productName is set
                    return dto;
                })
                .toList();

        return buildOrderResponse(order, paymentResponse, orderItemDtos);
    }


    private void validateStockAvailability(List<OrderItem> items, Map<String, ProductResponse> productMap) {
        for (OrderItem item : items) {
            ProductResponse product = productMap.get(item.getProductId());
            if (product == null) {
                throw new BusinessException(String.format(OrderServiceConstants.PRODUCT_NOT_FOUND, item.getProductId()));
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException(String.format(
                        OrderServiceConstants.PRODUCT_STOCK_INSUFFICIENT,
                        product.getName(), product.getStockQuantity(), item.getQuantity()));
            }
        }
    }

    private void updateStock(List<OrderItem> items, int multiplier) {
        List<StockUpdateRequest.StockUpdateItem> stockUpdates = items.stream()
                .map(item -> new StockUpdateRequest.StockUpdateItem(item.getProductId(), item.getQuantity() * multiplier))
                .toList();
        productClient.updateStock(new StockUpdateRequest(stockUpdates));
    }

    private PaymentResponse processPayment(Order order, OrderRequest request) {
        return paymentClient.processPayment(new PaymentRequest(
                order.getOrderId(), order.getUserId(), order.getTotalAmount(), request.getPaymentMethod())).getBody();
    }

    private OrderResponse buildOrderResponse(Order order, PaymentResponse paymentResponse, List<OrderItemDto> items) {
        return new OrderResponse(
                order.getOrderId(), order.getUserId(), items, order.getTotalAmount(), order.getStatus(),
                order.getShippingAddress(), order.getBillingAddress(), order.getOrderDate(), order.getUpdatedAt(), paymentResponse.getPaymentId());
    }

    @Override
    public OrderResponse getOrderDetails(String id) {
        log.info("Fetching order details for orderId: {}", id);
        return orderRepository.findById(id)
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .orElseThrow(() -> {
                    log.error(OrderServiceConstants.ORDER_NOT_FOUND_LOG, id);
                    return new BusinessException(String.format(OrderServiceConstants.ORDER_NOT_FOUND, id));
                });
    }

    @Override
    public List<OrderResponse> getAllOrderDetailsByUser(String userId) {
        log.info("Fetching all orders for userId: {}", userId);
        List<Order> orders = orderRepository.findAllByUserId(userId);

        if (orders.isEmpty()) {
            log.warn("No orders found for userId: {}", userId);
            throw new BusinessException(String.format(OrderServiceConstants.NO_ORDERS_FOUND_FOR_USER, userId));
        }

        return orders.stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .toList();
    }

    @Override
    public OrderResponse updateOrderDetails(String id, OrderRequest orderRequest) {
        log.info("Updating order details for orderId: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order with orderId {} not found", id);
                    return new BusinessException(String.format(OrderServiceConstants.ORDER_NOT_FOUND, id));
                });

        modelMapper.map(orderRequest, order);
        Order savedOrder = orderRepository.save(order);

        log.info("Order with orderId: {} updated successfully", id);
        return modelMapper.map(savedOrder, OrderResponse.class);
    }

    @Override
    public String cancelOrder(String id) {
        log.info(OrderServiceConstants.ORDER_CANCELLATION_INITIATED, id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(OrderServiceConstants.ORDER_NOT_FOUND_LOG, id);
                    return new BusinessException(String.format(OrderServiceConstants.ORDER_NOT_FOUND, id));
                });

        if (order.getStatus() == OrderStatus.DELIVERED) {
            log.warn("Cancellation attempt for already delivered order: {}", id);
            throw new BusinessException(OrderServiceConstants.ORDER_ALREADY_DELIVERED);
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn(OrderServiceConstants.ORDER_ALREADY_CANCELLED, id);
            throw new BusinessException(OrderServiceConstants.ORDER_ALREADY_CANCELLED);
        }

        log.info("Processing stock rollback for orderId: {}", id);
        updateStock(order.getItems(), 1);

        //TODO: Process refund (if applicable)

        log.info("Updating order status to CANCELED for orderId: {}", id);
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        //TODO : Notify the user about the cancellation (Optional)

        log.info("Order successfully canceled: {}", id);
        return OrderServiceConstants.ORDER_SUCCESSFULLY_CANCELLED;
    }

    @Override
    public void updateOrderStatus(String id, OrderStatus status) {
        Order order = orderRepository.findById(id).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
    }
}
