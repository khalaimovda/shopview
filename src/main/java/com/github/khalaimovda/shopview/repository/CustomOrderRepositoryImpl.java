package com.github.khalaimovda.shopview.repository;

import com.github.khalaimovda.shopview.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.dto.ProductOfOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.ArrayList;


@Repository
@RequiredArgsConstructor
public class CustomOrderRepositoryImpl implements CustomOrderRepository {

    private final DatabaseClient databaseClient;

    @Override
    public Flux<OrderWithProducts> findAllPlacedOrdersWithProducts() {
        return databaseClient.sql(
            """
            SELECT
              o.id AS order_id,
              p.id AS product_id,
              p.name AS product_name,
              p.price AS product_price,
              op.count AS product_count,
              p.price * op.count AS product_total_price
            FROM orders AS o
            LEFT JOIN order_product AS op
              ON o.id = op.order_id
            JOIN products AS p
              ON op.product_id = p.id
            WHERE o.is_active = false;
            """
        )
        .map((row, metadata) -> {
            OrderWithProducts order = new OrderWithProducts();
            order.setId(row.get("order_id", Long.class));

            order.setProducts(new ArrayList<>());
            if (row.get("product_id") != null) {
                ProductOfOrder product = new ProductOfOrder();
                product.setId(row.get("product_id", Long.class));
                product.setName(row.get("product_name", String.class));
                product.setPrice(row.get("product_price", BigDecimal.class));
                product.setCount(row.get("product_count", Integer.class));
                product.setTotalPrice(row.get("product_total_price", BigDecimal.class));
                order.getProducts().add(product);
            }

            return order;
        })
        .all()
        .groupBy(OrderWithProducts::getId)
        .flatMap(group -> group.reduce( // Collect products of one order
            (order1, order2) -> {
                order1.getProducts().addAll(order2.getProducts());
                return order1;
            }
        ));
    }
}
