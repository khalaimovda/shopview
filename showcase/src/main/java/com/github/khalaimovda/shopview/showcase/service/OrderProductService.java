package com.github.khalaimovda.shopview.showcase.service;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface OrderProductService {
    Mono<Map<Long, Integer>> getProductIdCountMap(Long orderId, List<Long> productIds);
    Mono<Void> addProductToOrder(Long orderId, Long productId);
    Mono<Void> decreaseProductInOrder(Long orderId, Long productId);
    Mono<Void> removeProductFromOrder(Long orderId, Long productId);
}
