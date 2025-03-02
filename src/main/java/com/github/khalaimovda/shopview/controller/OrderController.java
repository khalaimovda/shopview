package com.github.khalaimovda.shopview.controller;

import com.github.khalaimovda.shopview.service.OrderService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

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
}
