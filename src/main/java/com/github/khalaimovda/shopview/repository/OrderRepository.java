package com.github.khalaimovda.shopview.repository;

import com.github.khalaimovda.shopview.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    Mono<Order> findByIsActiveTrue();
    Flux<Order> findAllByIsActiveFalseOrderByIdDesc(); // todo: Нет загрузки связанных сущностей!
//    @EntityGraph(attributePaths = "orderProducts.product")
//    List<Order> findAllByIsActiveFalseOrderByIdDesc();
}
