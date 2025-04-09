package com.main.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal total;
}
