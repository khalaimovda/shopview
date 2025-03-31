package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.OrderProductId;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderProductServiceImpl implements OrderProductService {

    private final OrderProductRepository orderProductRepository;

    @Override
    public Map<Long, Integer> getProductIdCountMap(Order order, List<Product> products) {
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrderAndProductIn(order, products);
        return orderProducts.stream()
                .collect(Collectors.toMap(op -> op.getId().getProductId(), OrderProduct::getCount));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    public void addProductToOrder(Order order, Product product) {
        Optional<OrderProduct> orderProduct = orderProductRepository.findById(new OrderProductId(order.getId(), product.getId()));
        orderProduct.ifPresentOrElse(
            (op) -> {
                op.incrementCount();
                orderProductRepository.save(op);
            },
            () -> orderProductRepository.save(new OrderProduct(order, product, 1))
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    public void decreaseProductInOrder(Order order, Product product) {
        Optional<OrderProduct> orderProduct = orderProductRepository.findById(new OrderProductId(order.getId(), product.getId()));
        orderProduct.ifPresent((op) -> {
            if (op.getCount() > 1) {
                op.decrementCount();
                orderProductRepository.save(op);
            } else {
                orderProductRepository.delete(op);
            }
        });
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    public void removeProductFromOrder(Order order, Product product) {
        Optional<OrderProduct> orderProduct = orderProductRepository.findById(new OrderProductId(order.getId(), product.getId()));
        orderProduct.ifPresent(orderProductRepository::delete);
    }
}
