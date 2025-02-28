package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Page<Product> getAllProducts(String contentSubstring, Pageable pageable) {
        return productRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            contentSubstring, contentSubstring, pageable);
    }
}
