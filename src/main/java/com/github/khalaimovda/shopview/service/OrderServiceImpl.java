package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.OrderListItem;
import com.github.khalaimovda.shopview.dto.ProductOfOrder;
import com.github.khalaimovda.shopview.mapper.OrderMapper;
import com.github.khalaimovda.shopview.mapper.ProductMapper;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    @Override
    @Cacheable(value = "orders")
    public List<OrderListItem> getAllOrders() {
        List<Order> orders = orderRepository.findAllByIsActiveFalseOrderByIdDesc();
        return orders.stream().map(this::getOrderDetail).map(orderMapper::toOrderListItem).toList();
    }

    @Override
    @Cacheable(value = "orders", key = "#id")
    public Optional<OrderDetail> getOrderDetail(Long id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        return optionalOrder.map(this::getOrderDetail);
    }

    @Override
    @Cacheable(value = "orders", key = "#order.id")
    public OrderDetail getOrderDetail(Order order) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOderId(order.getId());
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            Product product = orderProduct.getProduct();
            ProductOfOrder cartProduct = productMapper.toProductOfOrder(product, orderProduct.getCount());
            orderDetail.getProducts().add(cartProduct);
            totalPrice = totalPrice.add(cartProduct.getTotalPrice());
        }
        orderDetail.getProducts().sort(Comparator.comparing(ProductOfOrder::getName));
        orderDetail.setTotalPrice(totalPrice);
        return orderDetail;
    }
}
