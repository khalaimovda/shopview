package com.github.khalaimovda.shopview.showcase.repository;

import com.github.khalaimovda.shopview.showcase.dto.OrderWithProducts;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomOrderRepository {
    Flux<OrderWithProducts> findAllPlacedOrdersWithProducts(Long userId);
    Mono<OrderWithProducts> findOrderWithProductsById(Long id);
}
