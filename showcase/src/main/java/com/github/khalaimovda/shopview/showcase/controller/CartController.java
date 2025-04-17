package com.github.khalaimovda.shopview.showcase.controller;

import com.github.khalaimovda.shopview.showcase.security.AuthenticatedUser;
import com.github.khalaimovda.shopview.showcase.service.CartService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("isAuthenticated()")
    public Mono<String> getCart(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
        return cartService
            .getCart(user.getId())
            .switchIfEmpty(Mono.defer(() -> {
                model.addAttribute("cart", null);
                return Mono.empty();
            }))
            .doOnNext(cart -> model.addAttribute("cart", cart))
            .thenReturn("cart");
    }

    @PostMapping("/add/{productId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> addProductToCart(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable("productId") @Min(1L) Long productId
    ) {
        return cartService.addProductToCart(productId, user.getId());
    }

    @PostMapping("/decrease/{productId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> decreaseProductInCart(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable("productId") @Min(1L) Long productId
    ) {
        return cartService.decreaseProductInCart(productId, user.getId());
    }

    @DeleteMapping("/remove/{productId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> removeProductFromCart(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable("productId") @Min(1L) Long productId
    ) {
        return cartService.removeProductFromCart(productId, user.getId());
    }

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> checkout(@AuthenticationPrincipal AuthenticatedUser user) {
        return cartService.checkout(user.getId());
    }
}
