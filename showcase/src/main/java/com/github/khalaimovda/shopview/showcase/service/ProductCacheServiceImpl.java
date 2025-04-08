package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.ProductListItem;
import com.github.khalaimovda.shopview.showcase.mapper.ProductMapper;
import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.Product;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import com.github.khalaimovda.shopview.showcase.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductCacheServiceImpl implements ProductCacheService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductService orderProductService;
    private final ImageService imageService;
    private final ProductMapper productMapper;

    @Override
    @Cacheable(value = "products", key = "'count' + ':' + #name + ':' + #description")
    public Mono<Integer> countProducts(String name, String description) {
        return productRepository
            .countByNameOrDescriptionContaining(name, description);
    }

    @Override
    @Cacheable(
        value = "products",
        key = "'listItems' + ':' + #name + ':' + #description + ':' + #limit + ':' + #offset"
    )
    public Mono<List<ProductListItem>> getProductItems(String name, String description, int limit, long offset) {
        Mono<List<Product>> monoProducts = productRepository
            .findByNameOrDescriptionContaining(name, description, limit, offset)
            .collectList();

        Mono<Optional<Order>> monoCart = orderRepository
            .findByIsActiveTrue()
            .map(Optional::of)
            .switchIfEmpty(Mono.just(Optional.empty()));

        return monoProducts.zipWith(monoCart)
            .flatMap(tuple -> {
                List<Product> products = tuple.getT1();
                Optional<Order> optionalCart = tuple.getT2();

                if (products.isEmpty()) {
                    return Mono.just(List.of());
                }

                if (optionalCart.isEmpty()) {
                    return Mono.just(processProductsWithMap(products, Collections.emptyMap()));
                }
                Order cart = optionalCart.get();

                return orderProductService
                    .getProductIdCountMap(cart.getId(), products.stream().map(Product::getId).toList())
                    .map(map -> products.stream().map(product -> {
                        Integer count = map.getOrDefault(product.getId(), 0);
                        String imageSrcPath = imageService.getImageSrcPath(product.getImagePath());
                        return productMapper.toProductListItem(product, imageSrcPath, count);
                    }).toList());
            });
    }

    private List<ProductListItem> processProductsWithMap(List<Product> products, Map<Long, Integer> map) {
        return products.stream().map(product -> {
            Integer count = map.getOrDefault(product.getId(), 0);
            String imageSrcPath = imageService.getImageSrcPath(product.getImagePath());
            return productMapper.toProductListItem(product, imageSrcPath, count);
        }).toList();
    }
}
