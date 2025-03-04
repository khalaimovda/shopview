package com.github.khalaimovda.shopview.controller;

import com.github.khalaimovda.shopview.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

//    @GetMapping("")
//    public String getAllOrders(
//        Model model,
//        @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
//        @RequestParam(value = "search", defaultValue = "") String search
//    ) {
//        Page<ProductListResponseDto> page = productService.getAllProducts(search, pageable);
//        model.addAttribute("page", page);
//        return "products";
//    }




}
