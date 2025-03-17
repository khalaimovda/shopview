package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;

import java.util.Optional;

public interface CartService {
    Optional<OrderDetail> getCart();
    void addProductToCart(Long productId);
    void decreaseProductInCart(Long productId);
    void removeProductFromCart(Long productId);
    void checkout();
}
