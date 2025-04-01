package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.dto.ProductDetail;
import com.github.khalaimovda.shopview.dto.ProductListItem;
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
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.*;

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

        Mono<List<Product>> products = productRepository
            .findByNameOrDescriptionContaining(contentSubstring, contentSubstring, limit, offset)
            .collectList();

        Mono<Long> totalCount = productRepository.countByNameOrDescriptionContaining(contentSubstring, contentSubstring);

        Mono<Order> activeOrder = orderRepository.findByIsActiveTrue();

        Mono<Map<Long, Integer>> productIdCountMap = activeOrder
            .flatMap(order -> products
                .flatMap(productList -> orderProductService
                    .getProductIdCountMap(order.getId(), productList.stream().map(Product::getId).toList())))
            .switchIfEmpty(Mono.just(new HashMap<>()));

        Mono<List<ProductListItem>> resultProducts = products
            .zipWith(productIdCountMap)
            .map(tuple -> {
                List<Product> productList = tuple.getT1();
                Map<Long, Integer> map = tuple.getT2();
                return productList.stream()
                    .map(product -> {
                        Integer count = map.getOrDefault(product.getId(), 0);
                        String imageSrcPath = imageService.getImageSrcPath(product.getImagePath());
                        return productMapper.toProductListItem(product,  imageSrcPath, count);
                    }).toList();
            });

        return resultProducts
            .zipWith(totalCount)
            .map(tuple -> {
                List<ProductListItem> productListItems = tuple.getT1();
                long totalElements = tuple.getT2();
                return new PageImpl<>(productListItems, pageable, totalElements);
            });
    }

    @Transactional
//    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Mono<Void> createProduct(ProductCreateForm form) {
        return imageService.saveImage(form.getImage())
            .onErrorComplete()
            .map(imagePath -> productMapper.toProduct(form, imagePath))
            .map(productRepository::save)
            .then();
    }

//    @Cacheable(value = "products", key = "#id")
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
