package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.OrderDetail;
import com.github.khalaimovda.shopview.showcase.dto.OrderListItem;
import com.github.khalaimovda.shopview.showcase.mapper.OrderMapper;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Cacheable(value = "orders", key = "'all'")
    @PreAuthorize("isAuthenticated() and #userId == principal.id")
    public Flux<OrderListItem> getAllOrders(Long userId) {
        return orderRepository
            .findAllPlacedOrdersWithProducts(userId)
            .map(orderMapper::toOrderListItem);
    }

    @Override
    @Cacheable(value = "orders", key = "#id")
    @PreAuthorize("isAuthenticated()")
    public Mono<OrderDetail> getOrderDetail(Long id) {
        return orderRepository
            .findOrderWithProductsById(id)
            .switchIfEmpty(Mono.error(new NoSuchElementException(String.format("Order with id %s not found", id))))
            .map(orderMapper::toOrderDetail);
    }
}
