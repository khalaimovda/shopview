package com.github.khalaimovda.shopview.controller;


import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.dto.ProductListResponseDto;
import com.github.khalaimovda.shopview.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("")
    public ResponseEntity<Void> createProduct(@Valid @ModelAttribute ProductCreateForm form) {
        productService.createProduct(form);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
