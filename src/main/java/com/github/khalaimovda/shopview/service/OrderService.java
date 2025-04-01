package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.OrderListItem;
import com.github.khalaimovda.shopview.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {
    Flux<OrderListItem> getAllOrders();
    Mono<OrderDetail> getOrderDetail(Long id);
    Mono<OrderDetail> getOrderDetail(Order order);
}
