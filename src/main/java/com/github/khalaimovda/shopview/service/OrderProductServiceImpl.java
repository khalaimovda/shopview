package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.OrderProductId;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import lombok.RequiredArgsConstructor;
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
    public Mono<Map<Long, Integer>> getProductIdCountMap(Order order, List<Product> products) {
        return orderProductRepository
            .findAllByOrderAndProductIn(order, products)
            .collect(Collectors.toMap(
                op -> op.getId().getProductId(),
                OrderProduct::getCount));
    }

    @Override
    @Transactional
//    @Caching(evict = {
//        @CacheEvict(value = "products", allEntries = true),
//        @CacheEvict(value = "orders", allEntries = true)
//    })
    public Mono<Void> addProductToOrder(Order order, Product product) {
        return orderProductRepository
            .findById(new OrderProductId(order.getId(), product.getId()))
            .flatMap(orderProduct -> {
                orderProduct.incrementCount();
                return orderProductRepository.save(orderProduct).then();
            })
            .switchIfEmpty(orderProductRepository.save(new OrderProduct(order, product, 1)).then());
    }

    @Override
    @Transactional
//    @Caching(evict = {
//        @CacheEvict(value = "products", allEntries = true),
//        @CacheEvict(value = "orders", allEntries = true)
//    })
    public Mono<Void> decreaseProductInOrder(Order order, Product product) {
        return orderProductRepository
            .findById(new OrderProductId(order.getId(), product.getId()))
            .flatMap(orderProduct -> {
                if (orderProduct.getCount() > 1) {
                    orderProduct.decrementCount();
                    return orderProductRepository.save(orderProduct).then();
                }
                return orderProductRepository.delete(orderProduct).then();
            });
    }

    @Override
    @Transactional
//    @Caching(evict = {
//        @CacheEvict(value = "products", allEntries = true),
//        @CacheEvict(value = "orders", allEntries = true)
//    })
    public Mono<Void> removeProductFromOrder(Order order, Product product) {
        return orderProductRepository
            .findById(new OrderProductId(order.getId(), product.getId()))
            .flatMap(orderProductRepository::delete);
    }
}
