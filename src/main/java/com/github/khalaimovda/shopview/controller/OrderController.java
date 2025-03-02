package com.github.khalaimovda.shopview.controller;

import com.github.khalaimovda.shopview.service.OrderService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/add/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public void addProductToCart(@PathVariable("productId") @Min(1L) Long productId) {
        orderService.addProductToCart(productId);
    }

    @PostMapping("/decrease/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public void decreaseProductInCart(@PathVariable("productId") @Min(1L) Long productId) {
        orderService.decreaseProductInCart(productId);
    }

    @DeleteMapping("/remove/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeProductFromCart(@PathVariable("productId") @Min(1L) Long productId) {
        orderService.removeProductFromCart(productId);
    }
}
