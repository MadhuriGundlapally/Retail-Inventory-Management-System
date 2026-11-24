package com.example.inventory.controller;

import com.example.inventory.dto.StockAdjustRequest;
import com.example.inventory.dto.StockResponse;
import com.example.inventory.service.StockService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    @GetMapping("/low")
    public List<StockResponse> lowStock() {
        return service.lowStock();
    }

    @GetMapping("/{productId}")
    public StockResponse get(@PathVariable Long productId) {
        return service.get(productId);
    }

    @PostMapping("/{productId}/adjust")
    public StockResponse adjust(@PathVariable Long productId, @Valid @RequestBody StockAdjustRequest req) {
        return service.adjust(productId, req);
    }
}
