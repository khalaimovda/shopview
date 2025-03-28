INSERT INTO orders(is_active)
SELECT (s = 10)
FROM generate_series(1, 10) s;