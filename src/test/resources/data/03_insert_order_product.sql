WITH numbered_orders AS (
    SELECT id, row_number() OVER (ORDER BY id) AS rn
    FROM orders
)
INSERT INTO order_product(order_id, product_id, count)
SELECT o.id, p.id, (o.rn + pc) AS count
FROM numbered_orders o
CROSS JOIN generate_series(0, 3) AS pc
JOIN products p ON p.name = 'Товар ' || (o.rn + pc + 3);