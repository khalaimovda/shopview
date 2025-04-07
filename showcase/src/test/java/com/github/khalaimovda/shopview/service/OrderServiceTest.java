package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.OrderListItem;
import com.github.khalaimovda.shopview.dto.OrderWithProducts;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Stream;

import static com.github.khalaimovda.shopview.utils.OrderUtils.*;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        List<OrderWithProducts> ordersWithProducts = generateRandomOrdersWithProducts(5);
        when(orderRepository.findAllPlacedOrdersWithProducts())
            .thenReturn(Flux.just(ordersWithProducts.toArray(new OrderWithProducts[0])));

        List<OrderListItem> expectedOrderListItems = ordersWithProducts.stream()
            .map(order -> {
                OrderListItem orderListItem = new OrderListItem();
                orderListItem.setId(order.getId());
                orderListItem.setPrice(calculateOrderPrice(order));
                return orderListItem;
            }).toList();

        // Act
        Flux<OrderListItem> fluxOrderListItems = orderService.getAllOrders();

        // Assert
        StepVerifier
            .create(fluxOrderListItems.collectList())
            .assertNext(orderListItems ->  assertEquals(expectedOrderListItems, orderListItems))
            .verifyComplete();

        verify(orderRepository, times(1)).findAllPlacedOrdersWithProducts();
    }

    @Test
    void testGetOrderDetail() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder();
        OrderWithProducts orderWithProducts = generateRandomOrderWithProducts(order.getId(), products);
        when(orderRepository.findOrderWithProductsById(anyLong())).thenReturn(Mono.just(orderWithProducts));
        OrderDetail expectedOrderDetail = getOrderDetail(orderWithProducts);

        // Act
        Mono<OrderDetail> monoOrderDetail = orderService.getOrderDetail(order.getId());

        // Assert
        StepVerifier
            .create(monoOrderDetail)
            .assertNext(orderDetail -> assertEquals(expectedOrderDetail, orderDetail))
            .verifyComplete();

        verify(orderRepository, times(1)).findOrderWithProductsById(order.getId());
    }
}
