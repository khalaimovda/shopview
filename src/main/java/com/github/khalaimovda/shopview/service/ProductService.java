package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.dto.ProductDetail;
import com.github.khalaimovda.shopview.dto.ProductListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface ProductService {
    Mono<Page<ProductListItem>> getAllProducts(String contentSubstring, Pageable pageable);
    Mono<Void> createProduct(ProductCreateForm form);
    Mono<ProductDetail> getProductById(Long id);
}
