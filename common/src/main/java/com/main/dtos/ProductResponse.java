package com.main.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {

    private String id;

    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private int stockQuantity;
    private String sku;
    private String brand;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
