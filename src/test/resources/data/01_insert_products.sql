INSERT INTO products(name, description, image_path, price)
SELECT 'Товар ' || s,
       'Описание ' || s,
       'image_path_' || s || '.png',
       0.99 + s
FROM generate_series(1, 25) s;