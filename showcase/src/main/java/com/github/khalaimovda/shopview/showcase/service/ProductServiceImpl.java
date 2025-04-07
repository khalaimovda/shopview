package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.showcase.dto.ProductDetail;
import com.github.khalaimovda.shopview.showcase.dto.ProductListItem;
import com.github.khalaimovda.shopview.showcase.mapper.ProductMapper;
import com.github.khalaimovda.shopview.showcase.model.OrderProduct;
import com.github.khalaimovda.shopview.showcase.model.Product;
import com.github.khalaimovda.shopview.showcase.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import com.github.khalaimovda.shopview.showcase.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductMapper productMapper;
    private final ImageService imageService;
    private final OrderProductService orderProductService;

    @Override
    @Transactional(readOnly = true)
    public Mono<Page<ProductListItem>> getAllProducts(String contentSubstring, Pageable pageable) {
        int limit = pageable.getPageSize();
        long offset = pageable.getOffset();

        return productRepository
            .countByNameOrDescriptionContaining(contentSubstring, contentSubstring)
            .flatMap(total -> productRepository
                .findByNameOrDescriptionContaining(contentSubstring, contentSubstring, limit, offset)
                .collectList()
                .flatMap(products -> {
                    if (products.isEmpty()) {
                        return Mono.just(new PageImpl<>(List.of(), pageable, total));
                    }
                    return orderRepository
                        .findByIsActiveTrue()
                        .flatMap(cart -> orderProductService
                            .getProductIdCountMap(cart.getId(), products.stream().map(Product::getId).toList())
                        )
                        .defaultIfEmpty(new HashMap<>())
                        .map(map -> {
                            List<ProductListItem> productListItems = products.stream().map(product -> {
                                Integer count = map.getOrDefault(product.getId(), 0);
                                String imageSrcPath = imageService.getImageSrcPath(product.getImagePath());
                                return productMapper.toProductListItem(product, imageSrcPath, count);
                            }).toList();
                            return new PageImpl<>(productListItems, pageable, total);
                        });
                })
            );
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Mono<Void> createProduct(ProductCreateForm form) {
        return imageService.saveImage(form.getImage())
            .map(imagePath -> productMapper.toProduct(form, imagePath))
            .flatMap(productRepository::save)
            .then();
    }

    @Cacheable(value = "products", key = "#id")
    @Override
    public Mono<ProductDetail> getProductById(Long id) {
        return productRepository
            .findById(id)
            .switchIfEmpty(Mono.error(new NoSuchElementException(String.format("Product with id %s not found", id))))
            .flatMap(
                product -> {
                    String imageSrcPath = imageService.getImageSrcPath(product.getImagePath());
                    return orderRepository
                        .findByIsActiveTrue()
                        .flatMap(
                            cart -> orderProductRepository
                                .findByOrderIdAndProductId(cart.getId(), product.getId())
                                .map(OrderProduct::getCount)
                                .defaultIfEmpty(0)
                                .map(count -> productMapper.toProductDetail(product, imageSrcPath, count))
                        )
                        .defaultIfEmpty(productMapper.toProductDetail(product, imageSrcPath, 0));
                }
            );
    }
}
