package com.main.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
public class OrderItem {
    @Id
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal total;
}
