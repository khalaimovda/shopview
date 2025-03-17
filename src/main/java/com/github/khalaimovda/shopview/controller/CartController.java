package com.github.khalaimovda.shopview.controller;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.service.CartService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("")
    public String getCart(Model model) {
        Optional<OrderDetail> cart = cartService.getCart();
        model.addAttribute("cart", cart.orElse(null));
        return "cart";
    }

    @PostMapping("/add/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public void addProductToCart(@PathVariable("productId") @Min(1L) Long productId) {
        cartService.addProductToCart(productId);
    }

    @PostMapping("/decrease/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public void decreaseProductInCart(@PathVariable("productId") @Min(1L) Long productId) {
        cartService.decreaseProductInCart(productId);
    }

    @DeleteMapping("/remove/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeProductFromCart(@PathVariable("productId") @Min(1L) Long productId) {
        cartService.removeProductFromCart(productId);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.OK)
    public void checkout() {
        cartService.checkout();
    }
}
