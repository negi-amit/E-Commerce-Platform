package com.main.dtos;

import lombok.Data;

@Data
public class PaymentResponse {
    private String paymentId;
    private String status;
}
