package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.OrderListItem;
import com.github.khalaimovda.shopview.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<OrderListItem> getAllOrders();
    Optional<OrderDetail> getOrderDetail(Long id);
    OrderDetail getOrderDetail(Order order);
}
