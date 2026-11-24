package com.example.inventory.dto;

import com.example.inventory.model.Product;
import java.math.BigDecimal;

public record ProductResponse(Long id, String sku, String name, String category, BigDecimal price,
                              int quantity, int reorderLevel) {
    public static ProductResponse from(Product p) {
        int qty = p.getStock() == null ? 0 : p.getStock().getQuantity();
        int reorder = p.getStock() == null ? 0 : p.getStock().getReorderLevel();
        return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getCategory(), p.getPrice(), qty, reorder);
    }
}
