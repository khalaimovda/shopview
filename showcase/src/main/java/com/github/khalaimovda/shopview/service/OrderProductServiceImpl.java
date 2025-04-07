package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderProductServiceImpl implements OrderProductService {

    private final OrderProductRepository orderProductRepository;

    @Override
    public Mono<Map<Long, Integer>> getProductIdCountMap(Long orderId, List<Long> productIds) {
        return orderProductRepository
            .findAllByOrderIdAndProductIdIn(orderId, productIds)
            .collect(Collectors.toMap(
                OrderProduct::getProductId,
                OrderProduct::getCount));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    public Mono<Void> addProductToOrder(Long orderId, Long productId) {
        return orderProductRepository
            .findByOrderIdAndProductId(orderId, productId)
            .flatMap(orderProduct -> {
                orderProduct.incrementCount();
                return orderProductRepository.save(orderProduct);
            })
            .switchIfEmpty(Mono.defer(
                () -> orderProductRepository.save(new OrderProduct(orderId, productId, 1)))
            )
            .then();
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    public Mono<Void> decreaseProductInOrder(Long orderId, Long productId) {
        return orderProductRepository
            .findByOrderIdAndProductId(orderId, productId)
            .flatMap(orderProduct -> {
                if (orderProduct.getCount() > 1) {
                    orderProduct.decrementCount();
                    return orderProductRepository.save(orderProduct);
                }
                return orderProductRepository.delete(orderProduct);
            })
            .then();
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    public Mono<Void> removeProductFromOrder(Long orderId, Long productId) {
        return orderProductRepository
            .findByOrderIdAndProductId(orderId, productId)
            .flatMap(orderProductRepository::delete);
    }
}
