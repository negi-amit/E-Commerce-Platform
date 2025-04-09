package com.main.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentRequest {

    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String paymentMethod;
}
