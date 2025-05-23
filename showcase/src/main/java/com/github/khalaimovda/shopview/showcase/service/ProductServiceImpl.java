package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.showcase.dto.ProductDetail;
import com.github.khalaimovda.shopview.showcase.dto.ProductListItem;
import com.github.khalaimovda.shopview.showcase.mapper.ProductMapper;
import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.OrderProduct;
import com.github.khalaimovda.shopview.showcase.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import com.github.khalaimovda.shopview.showcase.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductMapper productMapper;
    private final ImageService imageService;
    private final ProductCacheService productCacheService;

    @Override
//    @Transactional(readOnly = true)
    public Mono<Page<ProductListItem>> getAllProducts(String contentSubstring, Pageable pageable, Optional<Long> userId) {
        // We do not use cache here (because of page), we use it in ProductCacheService
        int limit = pageable.getPageSize();
        long offset = pageable.getOffset();

        Mono<Integer> total = productCacheService.countProducts(contentSubstring, contentSubstring);
        Mono<List<ProductListItem>> products = productCacheService.getProductItems(
            contentSubstring, contentSubstring, limit, offset, userId);

        return products.zipWith(total)
            .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    @Secured("ROLE_ADMIN")
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Mono<Void> createProduct(ProductCreateForm form) {
        return imageService.saveImage(form.getImage())
            .map(imagePath -> productMapper.toProduct(form, imagePath))
            .flatMap(productRepository::save)
            .then();
    }

    @Override
    @Cacheable(value = "products", key = "#id + ':' + (#userId.isPresent() ? #userId.get() : 'null')")
    public Mono<ProductDetail> getProductDetailById(Long id, Optional<Long> userId) {
        return productRepository
            .findById(id)
            .switchIfEmpty(Mono.error(new NoSuchElementException(String.format("Product with id %s not found", id))))
            .flatMap(
                product -> {
                    String imageSrcPath = imageService.getImageSrcPath(product.getImagePath());
                    Mono<Order> monoCart = userId
                        .map(orderRepository::findByUserIdAndIsActiveTrue)
                        .orElse(Mono.empty());

                    return monoCart
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
