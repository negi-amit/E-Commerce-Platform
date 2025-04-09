package com.main.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequestDto {
    @NotBlank(message = "Product ID must not be blank")
    private String productId;

    @Positive(message = "Quantity must be positive")
    private int quantity;
}
