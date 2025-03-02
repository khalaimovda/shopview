package com.github.khalaimovda.shopview.controller;


import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.dto.ProductListResponseDto;
import com.github.khalaimovda.shopview.dto.ProductResponseDto;
import com.github.khalaimovda.shopview.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
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
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(@Valid @ModelAttribute ProductCreateForm form) {
        productService.createProduct(form);
    }

    @GetMapping("/{id}")
    public String getProductById(Model model, @PathVariable("id") @Min(1L) Long id) {
        ProductResponseDto product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product";
    }
}
