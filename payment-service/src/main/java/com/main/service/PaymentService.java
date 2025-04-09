package com.main.service;

import com.main.dtos.PaymentRequest;
import com.main.dtos.PaymentResponse;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest paymentRequest);

    PaymentResponse retrievePaymentDetails(String id);
}
