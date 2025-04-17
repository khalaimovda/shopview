package com.github.khalaimovda.shopview.showcase.controller;


import com.github.khalaimovda.shopview.showcase.dto.OrderDetail;
import com.github.khalaimovda.shopview.showcase.dto.OrderListItem;
import com.github.khalaimovda.shopview.showcase.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.Product;
import com.github.khalaimovda.shopview.showcase.security.AuthenticatedUser;
import com.github.khalaimovda.shopview.showcase.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.github.khalaimovda.shopview.showcase.utils.OrderUtils.*;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProducts;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private Model model;

    @InjectMocks
    private OrderController orderController;

    private final AuthenticatedUser authenticatedUser = new AuthenticatedUser(
        42L, "testuser", "encodedPassword", List.of()
    );

    @Test
    void testGetAllOrders() throws Exception {
        List<OrderWithProducts> ordersWithProducts = generateRandomOrdersWithProducts(5);
        List<OrderListItem> orders = ordersWithProducts.stream()
            .map(order -> {
                OrderListItem orderListItem = new OrderListItem();
                orderListItem.setId(order.getId());
                orderListItem.setPrice(calculateOrderPrice(order));
                return orderListItem;
            }).toList();

        when(orderService.getAllOrders(anyLong()))
            .thenReturn(Flux.just(orders.toArray(new OrderListItem[0])));

        StepVerifier
            .create(orderController.getAllOrders(authenticatedUser, model))
            .expectNext("orders")
            .verifyComplete();

        verify(orderService, times(1)).getAllOrders(authenticatedUser.getId());
        verify(model).addAttribute("orders", orders);
    }

    @Test
    void testGetOrderById() throws Exception {
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder();
        OrderWithProducts orderWithProducts = generateRandomOrderWithProducts(order.getId(), products);
        OrderDetail orderDetail = getOrderDetail(orderWithProducts);

        when(orderService.getOrderDetail(anyLong())).thenReturn(Mono.just(orderDetail));

        StepVerifier
            .create(orderController.getOrderById(model, order.getId()))
            .expectNext("order")
            .verifyComplete();

        verify(orderService, times(1)).getOrderDetail(order.getId());
        verify(model).addAttribute("order", orderDetail);
    }
}
