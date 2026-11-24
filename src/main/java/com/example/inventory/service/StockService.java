package com.example.inventory.service;

import com.example.inventory.dto.StockAdjustRequest;
import com.example.inventory.dto.StockResponse;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.Stock;
import com.example.inventory.repository.StockRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);
    private final StockRepository stocks;

    public StockService(StockRepository stocks) {
        this.stocks = stocks;
    }

    @Transactional(readOnly = true)
    public StockResponse get(Long productId) {
        return StockResponse.from(find(productId));
    }

    public StockResponse adjust(Long productId, StockAdjustRequest req) {
        Stock stock = find(productId);
        int newQty = stock.getQuantity() + req.delta();
        if (newQty < 0) {
            throw new InsufficientStockException("Only " + stock.getQuantity()
                    + " in stock; cannot remove " + Math.abs(req.delta()));
        }
        stock.setQuantity(newQty);
        log.info("Stock adjusted productId={} delta={} newQty={} reason={}", productId, req.delta(), newQty, req.reason());
        return StockResponse.from(stock);
    }

    @Transactional(readOnly = true)
    public List<StockResponse> lowStock() {
        return stocks.findLowStock().stream().map(StockResponse::from).toList();
    }

    private Stock find(Long productId) {
        return stocks.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock for product " + productId + " not found"));
    }
}
