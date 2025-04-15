package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.OrderDetail;
import reactor.core.publisher.Mono;

public interface CartService {
    Mono<OrderDetail> getCart(Long userId);
    Mono<Void> addProductToCart(Long productId, Long userId);
    Mono<Void> decreaseProductInCart(Long productId, Long userId);
    Mono<Void> removeProductFromCart(Long productId, Long userId);
    Mono<Void> checkout(Long userId);
}
