package com.github.khalaimovda.shopview.controller;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.OrderListItem;
import com.github.khalaimovda.shopview.service.OrderService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @GetMapping("")
    public String getAllOrders(Model model) {
        List<OrderListItem> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/{id}")
    public String getOrderById(Model model, @PathVariable("id") @Min(1L) Long id) {
        Optional<OrderDetail> orderDetail = orderService.getOrderDetail(id);
        model.addAttribute("order", orderDetail.orElse(null));
        return "order";
    }
}
