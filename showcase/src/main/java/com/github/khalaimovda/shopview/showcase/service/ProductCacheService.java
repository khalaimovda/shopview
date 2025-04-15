package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.ProductListItem;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface ProductCacheService {
    Mono<Integer> countProducts(String name, String description);
    Mono<List<ProductListItem>> getProductItems(String name, String description, int limit, long offset, Optional<Long> userId);
}
