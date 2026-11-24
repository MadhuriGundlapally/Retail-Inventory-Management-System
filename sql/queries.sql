-- Useful MySQL queries for inventory management

-- 1. Products at or below reorder level (what the /api/stock/low endpoint returns)
SELECT p.id, p.sku, p.name, s.quantity, s.reorder_level
FROM products p JOIN stock s ON s.product_id = p.id
WHERE s.quantity <= s.reorder_level
ORDER BY s.quantity ASC;

-- 2. Stock value per category
SELECT p.category, SUM(s.quantity) AS units, SUM(s.quantity * p.price) AS stock_value
FROM products p JOIN stock s ON s.product_id = p.id
GROUP BY p.category
ORDER BY stock_value DESC;

-- 3. Out-of-stock products
SELECT p.sku, p.name FROM products p JOIN stock s ON s.product_id = p.id WHERE s.quantity = 0;

-- 4. Duplicate SKU sanity check (should return no rows; SKU is unique)
SELECT sku, COUNT(*) c FROM products GROUP BY sku HAVING c > 1;
