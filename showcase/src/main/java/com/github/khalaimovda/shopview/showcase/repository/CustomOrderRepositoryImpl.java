package com.github.khalaimovda.shopview.showcase.repository;

import com.github.khalaimovda.shopview.showcase.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.showcase.dto.ProductOfOrder;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;


@Repository
@RequiredArgsConstructor
public class CustomOrderRepositoryImpl implements CustomOrderRepository {

    private final DatabaseClient databaseClient;

    @Override
    public Flux<OrderWithProducts> findAllPlacedOrdersWithProducts(Long userId) {
        return databaseClient.sql(
            """
            SELECT
              o.id AS order_id,
              o.user_id AS user_id,
              p.id AS product_id,
              p.name AS product_name,
              p.price AS product_price,
              op.count AS product_count,
              p.price * op.count AS product_total_price
            FROM orders AS o
            LEFT JOIN order_product AS op
              ON o.id = op.order_id
            LEFT JOIN products AS p
              ON op.product_id = p.id
            WHERE
              o.user_id = :userId AND
              o.is_active = false;
            """
        )
        .bind("userId", userId)
        .map((row, metadata) -> convertRowToOrderWithProducts(row))
        .all()
        .groupBy(OrderWithProducts::getId)
        .flatMap(this::collectProductsOfOneOrder);
    }

    @Override
    public Mono<OrderWithProducts> findOrderWithProductsById(Long id) {
        return databaseClient.sql(
                """
                SELECT
                  o.id AS order_id,
                  o.user_id AS user_id,
                  p.id AS product_id,
                  p.name AS product_name,
                  p.price AS product_price,
                  op.count AS product_count,
                  p.price * op.count AS product_total_price
                FROM orders AS o
                LEFT JOIN order_product AS op
                  ON o.id = op.order_id
                LEFT JOIN products AS p
                  ON op.product_id = p.id
                WHERE o.id = :id;
                """
            )
            .bind("id", id)
            .map((row, metadata) -> convertRowToOrderWithProducts(row))
            .all()
            .groupBy(OrderWithProducts::getId)
            .flatMap(this::collectProductsOfOneOrder)
            .single();
    }

    private OrderWithProducts convertRowToOrderWithProducts(Row row) {
        OrderWithProducts order = new OrderWithProducts();
        order.setId(row.get("order_id", Long.class));
        order.setUserId(row.get("user_id", Long.class));

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
    }

    private Mono<OrderWithProducts> collectProductsOfOneOrder(GroupedFlux<Long, OrderWithProducts> group) {
        return group.reduce((order1, order2) -> {
            order1.getProducts().addAll(order2.getProducts());
            return order1;
        });
    }
}
