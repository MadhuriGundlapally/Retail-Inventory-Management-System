package com.example.inventory.dto;

import com.example.inventory.model.Stock;

public record StockResponse(Long productId, String sku, String name, int quantity, int reorderLevel, boolean lowStock) {
    public static StockResponse from(Stock s) {
        return new StockResponse(s.getProduct().getId(), s.getProduct().getSku(), s.getProduct().getName(),
                s.getQuantity(), s.getReorderLevel(), s.getQuantity() <= s.getReorderLevel());
    }
}
