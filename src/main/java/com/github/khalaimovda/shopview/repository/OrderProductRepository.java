package com.github.khalaimovda.shopview.repository;

import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.OrderProductId;
import com.github.khalaimovda.shopview.model.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface OrderProductRepository extends ReactiveCrudRepository<OrderProduct, OrderProductId> {
    Mono<OrderProduct> findByOrderAndProduct(Order order, Product product);
    Flux<OrderProduct> findAllByOrderAndProductIn(Order order, List<Product> products);
    Flux<OrderProduct> findAllByOrderIn(List<Order> orders);
}
