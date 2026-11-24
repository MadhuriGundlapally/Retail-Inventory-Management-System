package com.example.inventory.repository;

import com.example.inventory.model.Stock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductId(Long productId);

    @Query("select s from Stock s join fetch s.product p where s.quantity <= s.reorderLevel order by s.quantity asc")
    List<Stock> findLowStock();
}
