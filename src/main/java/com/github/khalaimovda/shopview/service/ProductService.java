package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.dto.ProductDetail;
import com.github.khalaimovda.shopview.dto.ProductListItem;
import com.github.khalaimovda.shopview.dto.CartProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductListItem> getAllProducts(String contentSubstring, Pageable pageable);
    void createProduct(ProductCreateForm form);
    ProductDetail getProductById(Long id);
}
