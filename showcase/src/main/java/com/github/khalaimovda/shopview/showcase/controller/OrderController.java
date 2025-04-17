package com.github.khalaimovda.shopview.showcase.controller;

import com.github.khalaimovda.shopview.showcase.security.AuthenticatedUser;
import com.github.khalaimovda.shopview.showcase.service.OrderService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("isAuthenticated()")
    public Mono<String> getAllOrders(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
        return orderService
            .getAllOrders(user.getId())
            .collectList()
            .doOnNext(orders -> model.addAttribute("orders", orders))
            .thenReturn("orders");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
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
