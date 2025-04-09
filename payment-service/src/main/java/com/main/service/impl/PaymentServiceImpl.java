package com.main.service.impl;

import com.main.dtos.OrderResponse;
import com.main.dtos.PaymentRequest;
import com.main.dtos.PaymentResponse;
import com.main.entity.Payment;
import com.main.exception.PaymentExceptions;
import com.main.exceptions.BusinessException;
import com.main.feign_client.OrderClient;
import com.main.repository.PaymentRepository;
import com.main.service.PaymentService;
import com.main.util.OrderStatus;
import com.main.util.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final OrderClient orderClient;
    private final Random random = new Random();

    public PaymentServiceImpl(PaymentRepository paymentRepository, ModelMapper modelMapper, OrderClient orderClient) {
        this.paymentRepository = paymentRepository;
        this.modelMapper = modelMapper;
        this.orderClient = orderClient;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        log.info("Initiating payment process for orderId: {}", paymentRequest.getOrderId());

        validatePaymentRequest(paymentRequest);
        Payment payment = modelMapper.map(paymentRequest, Payment.class);

        OrderResponse orderDetails = orderClient.getOrderDetails(payment.getOrderId());

        if (Objects.isNull(orderDetails)) {
            log.error("No order found with ID: {}. Please place an order first.", payment.getOrderId());
            throw new PaymentExceptions("No order found with ID: " + payment.getOrderId() + ". Please place an order first.");
        }

        log.info("Order verification successful for orderId: {}", payment.getOrderId());

        try {
            processMockPayment(payment);
            payment.setStatus(PaymentStatus.SUCCESS);
        } catch (BusinessException e) {
            log.warn("Payment failed for orderId: {}", payment.getOrderId());
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment); // Save the failed payment
            throw e; // Re-throw to notify the caller
        }

        log.info("Payment successful for orderId: {}. Updating payment details.", payment.getOrderId());

        payment.setTransactionDate(LocalDateTime.now());
        paymentRepository.save(payment);
        orderClient.updateOrderStatus(payment.getOrderId(), OrderStatus.PLACED.name());
        log.info("Order status updated to 'PAID' for orderId: {}", payment.getOrderId());

        // Return payment response
        return modelMapper.map(payment, PaymentResponse.class);
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            throw new BusinessException("Order ID is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Invalid payment amount", HttpStatus.BAD_REQUEST);
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            throw new BusinessException("Payment method is required", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Simulates a payment process (dummy logic).
     * In a real-world scenario, this method would integrate with a payment gateway.
     */
    private boolean processMockPayment(Payment payment) {
        log.info("Simulating payment processing for amount: {} and orderId: {}", payment.getAmount(), payment.getOrderId());
        return random.nextInt(10) < 9;
    }

    @Override
    public PaymentResponse retrievePaymentDetails(String orderId) {
        log.info("Retrieving payment details for orderId: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("No payment found for orderId: {}", orderId);
                    return new PaymentExceptions("Payment details not found for orderId: " + orderId);
                });

        log.info("Payment details retrieved successfully for orderId: {}", orderId);
        return modelMapper.map(payment, PaymentResponse.class);
    }

}
