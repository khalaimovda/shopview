package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.ProductListResponseDto;
import com.github.khalaimovda.shopview.mapper.ProductMapper;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductListResponseDto> getAllProducts(String contentSubstring, Pageable pageable) {

        Page<Product> productPage = productRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            contentSubstring, contentSubstring, pageable);

        Optional<Order> activeOrder = orderRepository.findByIsActiveTrue();
        Map<Long, Integer> productIdCountMap = activeOrder.map(order -> {
            List<OrderProduct> orderProducts = orderProductRepository.findAllByOrderAndProductIn(
                activeOrder.get(), productPage.getContent()
            );
            return orderProducts.stream()
                .collect(Collectors.toMap(op -> op.getId().getProductId(), op -> op.getCount()));
        }).orElseGet(HashMap::new);

        List<ProductListResponseDto> resultProducts = productPage.getContent().stream()
            .map(product -> {
                Integer count = productIdCountMap.getOrDefault(product.getId(), 0);
                return productMapper.toProductListResponseDto(product, count);
            }).toList();

        return new PageImpl<>(resultProducts, productPage.getPageable(), productPage.getTotalElements());
    }
}
