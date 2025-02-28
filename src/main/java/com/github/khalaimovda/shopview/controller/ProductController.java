package com.github.khalaimovda.shopview.controller;


import com.github.khalaimovda.shopview.dto.ProductListResponseDto;
import com.github.khalaimovda.shopview.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("")
    public String getAllProducts(
        Model model,
        @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(value = "search", defaultValue = "") String search
    ) {
        Page<ProductListResponseDto> page = productService.getAllProducts(search, pageable);
        model.addAttribute("page", page);
        return "products";
    }
}
