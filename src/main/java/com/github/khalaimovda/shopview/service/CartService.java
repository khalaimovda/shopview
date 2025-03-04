package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.Cart;

import java.util.Optional;

public interface CartService {
    Optional<Cart> getCart();
    void addProductToCart(Long productId);
    void decreaseProductInCart(Long productId);
    void removeProductFromCart(Long productId);
}
