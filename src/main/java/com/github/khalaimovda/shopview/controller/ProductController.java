package com.github.khalaimovda.shopview.controller;


import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.dto.ProductListItem;
import com.github.khalaimovda.shopview.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping("")
    public Mono<String> getAllProducts(
        Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "search", defaultValue = "") String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<ProductListItem> productPage = productService.getAllProducts(search, pageable);
        return Mono.just(productPage)
            .doOnNext(p -> model.addAttribute("page", p))
            .map(p -> "products");
    }

    @PostMapping(path = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> createProduct(@Valid @ModelAttribute ProductCreateForm form) {
        productService.createProduct(form);
        return Mono.empty();
    }

    @GetMapping("/{id}")
    public Mono<String> getProductById(Model model, @PathVariable("id") @Min(1L) Long id) {
        return Mono.fromSupplier(() -> productService.getProductById(id))
            .doOnNext(productDetail -> model.addAttribute("product", productDetail))
            .map(productDetail -> "product");
    }

}
