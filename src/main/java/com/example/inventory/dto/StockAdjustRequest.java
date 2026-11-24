package com.example.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** delta > 0 receives stock, delta < 0 removes stock (sale, damage, ...). */
public record StockAdjustRequest(@NotNull Integer delta, @Size(max = 200) String reason) {
}
