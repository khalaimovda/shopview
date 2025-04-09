package com.github.khalaimovda.shopview.showcase.controller;


import com.github.khalaimovda.shopview.showcase.dto.OrderDetail;
import com.github.khalaimovda.shopview.showcase.dto.OrderListItem;
import com.github.khalaimovda.shopview.showcase.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.showcase.mapper.PaymentMapperImpl;
import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.Product;
import com.github.khalaimovda.shopview.showcase.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.github.khalaimovda.shopview.showcase.utils.OrderUtils.*;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


@WebFluxTest(OrderController.class)
@Import({PaymentMapperImpl.class})
public class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

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

        when(orderService.getAllOrders())
            .thenReturn(Flux.just(orders.toArray(new OrderListItem[0])));

        webTestClient
            .get()
            .uri("/orders")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_HTML)
            .expectBody(String.class)
            .consumeWith(response -> {
                String body = response.getResponseBody();
                assertNotNull(body);
                assertTrue(body.contains("orders"));
            });

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void testGetOrderById() throws Exception {
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder();
        OrderWithProducts orderWithProducts = generateRandomOrderWithProducts(order.getId(), products);
        OrderDetail orderDetail = getOrderDetail(orderWithProducts);

        when(orderService.getOrderDetail(anyLong())).thenReturn(Mono.just(orderDetail));

        webTestClient
            .get()
            .uri("/orders/" + order.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_HTML)
            .expectBody(String.class)
            .consumeWith(response -> {
                String body = response.getResponseBody();
                assertNotNull(body);
                assertTrue(body.contains("products"));
            });

        verify(orderService, times(1)).getOrderDetail(order.getId());
    }
}
