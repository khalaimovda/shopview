package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
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

    @Override
    @Transactional
//    @Cacheable(value = "orders", key = "'cart'")
    public Mono<OrderDetail> getCart() {
        return orderRepository
            .findByIsActiveTrue()
            .flatMap(cart -> orderService.getOrderDetail(cart.getId()));
    }

    @Override
    @Transactional
//    @Caching(evict = {
//        @CacheEvict(value = "products", allEntries = true),
//        @CacheEvict(value = "orders", allEntries = true)
//    })
    public Mono<Void> addProductToCart(Long productId) {
        return getProductByIdOrNoSuchElementException(productId)
            .flatMap(product -> getOrCreateActiveOrder()
                .flatMap(cart -> orderProductService.addProductToOrder(cart, product)));
    }

    @Override
    @Transactional
//    @Caching(evict = {
//        @CacheEvict(value = "products", allEntries = true),
//        @CacheEvict(value = "orders", allEntries = true)
//    })
    public Mono<Void> decreaseProductInCart(Long productId) {
        return getProductByIdOrNoSuchElementException(productId)
            .flatMap(product -> getActiveOrderOrNoSuchElementException()
                .flatMap(cart -> orderProductService.decreaseProductInOrder(cart, product)));
    }

    @Override
    @Transactional
//    @Caching(evict = {
//        @CacheEvict(value = "products", allEntries = true),
//        @CacheEvict(value = "orders", allEntries = true)
//    })
    public Mono<Void> removeProductFromCart(Long productId) {
        return getProductByIdOrNoSuchElementException(productId)
            .flatMap(product -> getActiveOrderOrNoSuchElementException()
                .flatMap(cart -> orderProductService.removeProductFromOrder(cart, product)));
    }

    @Override
    @Transactional
//    @Caching(evict = {
//        @CacheEvict(value = "products", allEntries = true),
//        @CacheEvict(value = "orders", allEntries = true)
//    })
    public Mono<Void> checkout() {
        return orderRepository
            .findByIsActiveTrue()
            .map(cart -> {
                validateCartIsNotEmpty(cart); // todo: Доработать, когда orderService будет исправлен
                return cart;
            }).flatMap(
                cart -> {
                    cart.setIsActive(false); // checkout
                    return orderRepository.save(cart).then();
                }
            );
    }

    @Transactional
    private Mono<Order> getOrCreateActiveOrder() {
        return orderRepository
            .findByIsActiveTrue()
            .switchIfEmpty(orderRepository.save(new Order()));
    }

    private Mono<Product> getProductByIdOrNoSuchElementException(Long productId) {
        return productRepository.findById(productId)
            .switchIfEmpty(Mono.error(
                new NoSuchElementException(String.format("Product with id %s is not found", productId))));
    }

    private Mono<Order> getActiveOrderOrNoSuchElementException() {
        return orderRepository
            .findByIsActiveTrue()
            .switchIfEmpty(Mono.error(new NoSuchElementException("Active order is not found")));
    }

    private void validateCartIsNotEmpty(Order order) {
        OrderDetail orderDetail = orderService.getOrderDetail(order.getId()).block();
        if (orderDetail.getTotalPrice().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Cart is empty");
        }
    }
}
