DELETE FROM order_product;
DELETE FROM orders;
DELETE FROM products;

ALTER SEQUENCE products_id_seq RESTART WITH 1;
ALTER SEQUENCE orders_id_seq RESTART WITH 1;
