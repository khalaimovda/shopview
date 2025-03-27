package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.OrderListItem;
import com.github.khalaimovda.shopview.mapper.OrderMapper;
import com.github.khalaimovda.shopview.mapper.ProductMapper;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.khalaimovda.shopview.utils.OrderUtils.*;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Spy
    private ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Spy
    private OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @InjectMocks
    OrderServiceImpl orderService;

    @Test
    void testGetAllOrders() {
        // Arrange
        List<List<Product>> listOfProducts = Stream.generate(() -> generateRandomProducts(5)).limit(5).toList();

        List<Order> orders = generateRandomOrders(listOfProducts);
        when(orderRepository.findAllByIsActiveFalseOrderByIdDesc()).thenReturn(orders);

        List<OrderListItem> expectedOrderListItems = orders.stream()
            .map(order -> {
                OrderListItem orderListItem = new OrderListItem();
                orderListItem.setId(order.getId());
                orderListItem.setPrice(calculateOrderPrice(order));
                return orderListItem;
            }).toList();

        // Act
        List<OrderListItem> orderListItems = orderService.getAllOrders();

        // Assert
        verify(orderRepository, times(1)).findAllByIsActiveFalseOrderByIdDesc();
        assertEquals(expectedOrderListItems, orderListItems);
    }

    @Test
    void testGetOrderDetailById() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder(products);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        OrderDetail expectedOrderDetail = getOrderDetail(order);

        // Act
        Optional<OrderDetail> optionalOrderDetail = orderService.getOrderDetail(order.getId());

        // Assert
        verify(orderRepository, times(1)).findById(order.getId());

        assertTrue(optionalOrderDetail.isPresent());

        OrderDetail orderDetail = optionalOrderDetail.get();
        assertEquals(expectedOrderDetail, orderDetail);
    }

    @Test
    void testGetOrderDetailByOrder() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder(products);
        OrderDetail expectedOrderDetail = getOrderDetail(order);

        // Act
        OrderDetail orderDetail = orderService.getOrderDetail(order);

        // Assert
        assertEquals(expectedOrderDetail, orderDetail);
    }
}
