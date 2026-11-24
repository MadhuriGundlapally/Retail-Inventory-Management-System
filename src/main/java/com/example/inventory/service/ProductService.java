package com.example.inventory.service;

import com.example.inventory.dto.ProductRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.exception.DuplicateSkuException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.Product;
import com.example.inventory.model.Stock;
import com.example.inventory.repository.ProductRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public ProductResponse create(ProductRequest req) {
        if (products.existsBySku(req.sku())) {
            throw new DuplicateSkuException(req.sku());
        }
        Product p = new Product();
        apply(p, req);
        Stock stock = new Stock();
        stock.setProduct(p);
        stock.setQuantity(req.initialQuantity() == null ? 0 : req.initialQuantity());
        stock.setReorderLevel(req.reorderLevel() == null ? 0 : req.reorderLevel());
        p.setStock(stock);
        Product saved = products.save(p);
        log.info("Created product id={} sku={}", saved.getId(), saved.getSku());
        return ProductResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(String category) {
        List<Product> found = (category == null || category.isBlank())
                ? products.findAll() : products.findByCategoryIgnoreCase(category);
        return found.stream().map(ProductResponse::from).toList();
    }

    public ProductResponse update(Long id, ProductRequest req) {
        Product p = find(id);
        if (products.existsBySkuAndIdNot(req.sku(), id)) {
            throw new DuplicateSkuException(req.sku());
        }
        apply(p, req);
        if (req.reorderLevel() != null && p.getStock() != null) {
            p.getStock().setReorderLevel(req.reorderLevel());
        }
        log.info("Updated product id={}", id);
        return ProductResponse.from(p);
    }

    public void delete(Long id) {
        products.delete(find(id));
        log.info("Deleted product id={}", id);
    }

    private Product find(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
    }

    private void apply(Product p, ProductRequest req) {
        p.setSku(req.sku().trim());
        p.setName(req.name().trim());
        p.setCategory(req.category());
        p.setPrice(req.price());
    }
}
