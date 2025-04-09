package com.main.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StockUpdateRequest {
    private List<StockUpdateItem> stockUpdates;

    @Data
    @AllArgsConstructor
    public static class StockUpdateItem {
        private String productId;
        private int quantityChange;
    }
}
