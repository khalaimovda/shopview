package com.github.khalaimovda.shopview.controller;

import com.github.khalaimovda.shopview.service.CartService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("")
    public Mono<String> getCart(Model model) {
        return cartService
            .getCart()
            .switchIfEmpty(Mono.defer(() -> {
                model.addAttribute("cart", null);
                return Mono.empty();
            }))
            .doOnNext(cart -> model.addAttribute("cart", cart))
            .thenReturn("cart");
    }

    @PostMapping("/add/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> addProductToCart(@PathVariable("productId") @Min(1L) Long productId) {
        return cartService.addProductToCart(productId);
    }

    @PostMapping("/decrease/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> decreaseProductInCart(@PathVariable("productId") @Min(1L) Long productId) {
        return cartService.decreaseProductInCart(productId);
    }

    @DeleteMapping("/remove/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> removeProductFromCart(@PathVariable("productId") @Min(1L) Long productId) {
        return cartService.removeProductFromCart(productId);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> checkout() {
        return cartService.checkout();
    }
}
