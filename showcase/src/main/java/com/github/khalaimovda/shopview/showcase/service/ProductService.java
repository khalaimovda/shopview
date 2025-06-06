package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.showcase.dto.ProductDetail;
import com.github.khalaimovda.shopview.showcase.dto.ProductListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ProductService {
    Mono<Page<ProductListItem>> getAllProducts(String contentSubstring, Pageable pageable, Optional<Long> userId);
    Mono<Void> createProduct(ProductCreateForm form);
    Mono<ProductDetail> getProductDetailById(Long id, Optional<Long> userId);
}
