package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.dto.ProductListResponseDto;
import com.github.khalaimovda.shopview.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductListResponseDto> getAllProducts(String contentSubstring, Pageable pageable);
    void createProduct(ProductCreateForm form);
    ProductResponseDto getProductById(Long id);
}
