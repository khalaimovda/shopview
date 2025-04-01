package com.github.khalaimovda.shopview.repository;

import com.github.khalaimovda.shopview.dto.OrderWithProducts;
import reactor.core.publisher.Flux;

public interface CustomOrderRepository {
    Flux<OrderWithProducts> findAllPlacedOrdersWithProducts();
}
