package com.github.khalaimovda.shopview.showcase.repository;

import com.github.khalaimovda.shopview.showcase.model.OrderProduct;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface OrderProductRepository extends ReactiveCrudRepository<OrderProduct, Long> {

    // Auto query building did not work for some reason
    @Query("SELECT * FROM order_product WHERE order_id = :orderId AND product_id IN (:productIds)")
    Flux<OrderProduct> findAllByOrderIdAndProductIdIn(Long orderId, @Param("productIds") List<Long> productIds);

    @Query("SELECT * FROM order_product WHERE order_id = :orderId")
    Flux<OrderProduct> findAllByOrderId(Long orderId);

    @Query("SELECT * FROM order_product WHERE order_id = :orderId AND product_id = :productId")
    Mono<OrderProduct> findByOrderIdAndProductId(Long orderId, Long productId);
}
