package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.OrderDetail;
import com.github.khalaimovda.shopview.showcase.dto.OrderListItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {
    Flux<OrderListItem> getAllOrders();
    Mono<OrderDetail> getOrderDetail(Long id);
}
