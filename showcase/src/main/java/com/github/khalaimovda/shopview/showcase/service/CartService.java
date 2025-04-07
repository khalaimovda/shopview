package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.OrderDetail;
import reactor.core.publisher.Mono;

public interface CartService {
    Mono<OrderDetail> getCart();
    Mono<Void> addProductToCart(Long productId);
    Mono<Void> decreaseProductInCart(Long productId);
    Mono<Void> removeProductFromCart(Long productId);
    Mono<Void> checkout();
}
