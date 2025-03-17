package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.OrderListItem;
import com.github.khalaimovda.shopview.dto.ProductOfOrder;
import com.github.khalaimovda.shopview.mapper.ProductMapper;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductMapper productMapper;

    @Override
    public List<OrderListItem> getAllOrders() {
        // todo: Вот тут надо подумать, как затянуть
        List<Order> orders = orderRepository.findAll();

        // todo: Здесь явно лучше сразу писать сырой cql
        // todo: Соритровка по убыванию orderId
//        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrderContaining(orders);
//        orders.stream().map()

        // todo: Нужно будет сделать mapper, но пока ручками
        return orders.stream().map(order -> {
            OrderListItem orderListItem = new OrderListItem();
            orderListItem.setId(order.getId());
            orderListItem.setPrice(new BigDecimal("13.25")); // todo: Считать нормально
            return orderListItem;
        }).toList();
    }

    @Override
    public Optional<OrderDetail> getOrderDetail(Long id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        return optionalOrder.map(this::getOrderDetail);
    }

    @Override
    public OrderDetail getOrderDetail(Order order) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOderId(order.getId());
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            Product product = orderProduct.getProduct();
            ProductOfOrder cartProduct = productMapper.toCartProduct(product, orderProduct.getCount());
            orderDetail.getProducts().add(cartProduct);
            totalPrice = totalPrice.add(cartProduct.getTotalPrice());
        }
        orderDetail.getProducts().sort(Comparator.comparing(ProductOfOrder::getName));
        orderDetail.setTotalPrice(totalPrice);
        return orderDetail;
    }
}
