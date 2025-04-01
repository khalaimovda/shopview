package com.github.khalaimovda.shopview.repository;

import com.github.khalaimovda.shopview.dto.OrderWithProducts;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomOrderRepository {
    Flux<OrderWithProducts> findAllPlacedOrdersWithProducts();
    Mono<OrderWithProducts> findOrderWithProductsById(Long id);
}
