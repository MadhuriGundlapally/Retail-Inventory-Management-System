package com.example.inventory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.inventory.dto.StockAdjustRequest;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.Product;
import com.example.inventory.model.Stock;
import com.example.inventory.repository.StockRepository;
import com.example.inventory.service.StockService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StockServiceTest {

    private final StockRepository repo = mock(StockRepository.class);
    private final StockService service = new StockService(repo);

    private Stock stock(int qty) {
        Product p = new Product();
        p.setSku("A");
        p.setName("A");
        p.setPrice(BigDecimal.ONE);
        Stock s = new Stock();
        s.setProduct(p);
        s.setQuantity(qty);
        return s;
    }

    @Test
    void adjustThrowsWhenStockWouldGoNegative() {
        when(repo.findByProductId(1L)).thenReturn(Optional.of(stock(2)));
        assertThrows(InsufficientStockException.class, () -> service.adjust(1L, new StockAdjustRequest(-5, "sale")));
    }

    @Test
    void adjustUpdatesQuantity() {
        Stock s = stock(2);
        when(repo.findByProductId(1L)).thenReturn(Optional.of(s));
        assertEquals(7, service.adjust(1L, new StockAdjustRequest(5, "restock")).quantity());
    }

    @Test
    void missingStockThrowsNotFound() {
        when(repo.findByProductId(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.get(9L));
    }
}
