package com.github.khalaimovda.shopview.service;

public interface OrderService {
    void addProductToCart(Long productId);
    void decreaseProductInCart(Long productId);
    void removeProductFromCart(Long productId);
}
