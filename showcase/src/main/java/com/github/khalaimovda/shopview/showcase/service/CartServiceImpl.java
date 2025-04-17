package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.OrderDetail;
import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.Product;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import com.github.khalaimovda.shopview.showcase.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.NoSuchElementException;


@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderProductService orderProductService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Override
    @Transactional
    @Cacheable(value = "orders", key = "'cart'")
    @PreAuthorize("isAuthenticated() and #userId == principal.id")
    @PostAuthorize("returnObject.userId == principal.id")
    public Mono<OrderDetail> getCart(Long userId) {
        return orderRepository
            .findByUserIdAndIsActiveTrue(userId)
            .flatMap(cart -> orderService.getOrderDetail(cart.getId()));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    @PreAuthorize("isAuthenticated() and #userId == principal.id")
    public Mono<Void> addProductToCart(Long productId, Long userId) {
        return getProductByIdOrNoSuchElementException(productId)
            .flatMap(product -> getOrCreateActiveOrder(userId)
            .flatMap(cart -> orderProductService.addProductToOrder(cart.getId(), product.getId())));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    @PreAuthorize("isAuthenticated() and #userId == principal.id")
    public Mono<Void> decreaseProductInCart(Long productId, Long userId) {
        return getProductByIdOrNoSuchElementException(productId)
            .flatMap(product -> getActiveOrderOrNoSuchElementException(userId)
            .flatMap(cart -> orderProductService.decreaseProductInOrder(cart.getId(), product.getId())));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    @PreAuthorize("isAuthenticated() and #userId == principal.id")
    public Mono<Void> removeProductFromCart(Long productId, Long userId) {
        return getProductByIdOrNoSuchElementException(productId)
            .flatMap(product -> getActiveOrderOrNoSuchElementException(userId)
            .flatMap(cart -> orderProductService.removeProductFromOrder(cart.getId(), product.getId())));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    @PreAuthorize("isAuthenticated() and #userId == principal.id")
    public Mono<Void> checkout(Long userId) {
        return getActiveOrderOrNoSuchElementException(userId)
            .flatMap(cart -> orderService
                .getOrderDetail(cart.getId())
                .flatMap(orderDetail -> {
                    BigDecimal totalPrice = orderDetail.getTotalPrice();
                    if (totalPrice.compareTo(BigDecimal.ZERO) == 0) {
                        return Mono.error(new IllegalStateException("Cart is empty"));
                    }
                    return paymentService
                        .makePayment(userId, totalPrice)
                        .then(Mono.just(cart));
                })
            )
            .flatMap(cart -> {
                cart.setIsActive(false); // checkout
                return orderRepository.save(cart);
            })
            .then();
    }

    private Mono<Order> getOrCreateActiveOrder(Long userId) {
        return orderRepository
            .findByUserIdAndIsActiveTrue(userId)
            .switchIfEmpty(Mono.defer(() -> {
                Order order = new Order();
                order.setIsActive(true);
                order.setUserId(userId);
                return orderRepository.save(order);
            }));
    }

    private Mono<Product> getProductByIdOrNoSuchElementException(Long productId) {
        return productRepository.findById(productId)
            .switchIfEmpty(Mono.error(
                new NoSuchElementException(String.format("Product with id %s is not found", productId))));
    }

    private Mono<Order> getActiveOrderOrNoSuchElementException(Long userId) {
        return orderRepository
            .findByUserIdAndIsActiveTrue(userId)
            .switchIfEmpty(Mono.error(new NoSuchElementException("Active order is not found for userId: " + userId)));
    }
}
