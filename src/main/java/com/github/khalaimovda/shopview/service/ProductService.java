package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<Product> getAllProducts(String contentSubstring, Pageable pageable);
}
