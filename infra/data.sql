DO

$FILL_DATA$
DECLARE
  order_id BIGINT;
  is_active BOOLEAN;
BEGIN

  -- Create admin
  INSERT INTO users(username, password, roles)
  VALUES ('admin', '$2a$10$pY6ODS4d.d3703hEN0Gv5O0PXS0tNEEgN/TjTDiz6ZVkGUMREJvq.', '{"ADMIN"}');

  -- Set balance for admin
  INSERT INTO balance(user_id, balance)
  SELECT id, 300.0 FROM users WHERE username = 'admin';

  -- Create products
  FOR counter IN 1..25 LOOP
    RAISE NOTICE 'Inserting Product %', counter;

    EXECUTE
    $$
      INSERT INTO products(name, description, image_path, price)
      VALUES ('Товар ' || $1, 'Описание ' || $1, 'image_path_' || $1 || '.png', 0.99 + $1)
    $$
    USING counter;
  END LOOP;

  -- Orders with products
  FOR counter IN 1..10 LOOP
    RAISE NOTICE 'Inserting Order %', counter;

    is_active := (counter = 10); -- Last order will be active
    EXECUTE
    $$
      INSERT INTO orders(is_active, user_id)
      SELECT $1, id FROM users WHERE username = 'admin'
      RETURNING id
    $$
    INTO order_id
    USING is_active;

    RAISE NOTICE 'Fill Order % with Products', counter;
    FOR product_counter IN 0..3 LOOP
        EXECUTE
        $$
          INSERT INTO order_product(order_id, product_id, count)
          SELECT $1, id, ($2 + $3)
          FROM products WHERE name = 'Товар ' || ($2 + $3 + 3)
        $$
        USING order_id, counter, product_counter;
    END LOOP;
  END LOOP;

END;
$FILL_DATA$
