package com.github.khalaimovda.shopview.repository;

import com.github.khalaimovda.shopview.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long>, CustomOrderRepository {
    Mono<Order> findByIsActiveTrue();
}
