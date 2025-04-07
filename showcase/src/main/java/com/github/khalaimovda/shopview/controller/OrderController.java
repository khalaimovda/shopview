package com.github.khalaimovda.shopview.controller;

import com.github.khalaimovda.shopview.service.OrderService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @GetMapping("")
    public Mono<String> getAllOrders(Model model) {
        return orderService
            .getAllOrders()
            .collectList()
            .doOnNext(orders -> model.addAttribute("orders", orders))
            .thenReturn("orders");
    }

    @GetMapping("/{id}")
    public Mono<String> getOrderById(Model model, @PathVariable("id") @Min(1L) Long id) {
        return orderService
            .getOrderDetail(id)
            .switchIfEmpty(Mono.defer(() -> {
                model.addAttribute("order", null);
                return Mono.empty();
            }))
            .doOnNext(order -> model.addAttribute("order", order))
            .thenReturn("order");
    }
}
