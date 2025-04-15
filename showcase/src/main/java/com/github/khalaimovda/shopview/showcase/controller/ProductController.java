package com.github.khalaimovda.shopview.showcase.controller;


import com.github.khalaimovda.shopview.showcase.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.showcase.security.AuthenticatedUser;
import com.github.khalaimovda.shopview.showcase.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping("")
    public Mono<String> getAllProducts(
        Model model,
        @AuthenticationPrincipal AuthenticatedUser user,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "search", defaultValue = "") String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return productService
            .getAllProducts(search, pageable, Optional.ofNullable(user).map(AuthenticatedUser::getId))
            .doOnNext(productPage -> model.addAttribute("page", productPage))
            .thenReturn("products");
    }

    @PostMapping(path = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> createProduct(@Valid @ModelAttribute ProductCreateForm form) {
        return productService.createProduct(form);
    }

    @GetMapping("/{productId}")
    public Mono<String> getProductById(
        Model model,
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable("productId") @Min(1L) Long productId
    ) {
        return productService
            .getProductDetailById(productId, Optional.ofNullable(user).map(AuthenticatedUser::getId))
            .doOnNext(product -> model.addAttribute("product", product))
            .thenReturn("product");
    }
}
