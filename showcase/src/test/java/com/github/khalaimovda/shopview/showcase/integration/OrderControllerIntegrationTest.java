package com.github.khalaimovda.shopview.showcase.integration;


import com.github.khalaimovda.shopview.showcase.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.showcase.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import com.github.khalaimovda.shopview.showcase.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static com.github.khalaimovda.shopview.showcase.utils.OrderUtils.calculateOrderPrice;
import static org.junit.jupiter.api.Assertions.*;

public class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testGetAllOrders() throws Exception {
        Long orderId = orderRepository.findById(1L).block().getId();
        OrderWithProducts order = orderRepository.findOrderWithProductsById(orderId).block();
        BigDecimal totalPrice = calculateOrderPrice(order);

        webTestClient.get()
            .uri("/orders")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_HTML)
            .expectBody(String.class)
            .consumeWith(response -> {
                String body = response.getResponseBody();
                assertNotNull(body);
                assertTrue(body.contains("orders"));
                assertFalse(body.contains("Нет оформленных заказов"));
                assertTrue(body.contains("Заказ #1"));
                assertTrue(body.contains(totalPrice + " ₽"));
            });
    }

    @Test
    void testGetOrderById() throws Exception {
        Long orderId = orderRepository.findById(1L).block().getId();
        OrderWithProducts order = orderRepository.findOrderWithProductsById(orderId).block();
        BigDecimal totalPrice = calculateOrderPrice(order);

        webTestClient.get()
            .uri("/orders/" + orderId)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_HTML)
            .expectBody(String.class)
            .consumeWith(response -> {
                String body = response.getResponseBody();
                assertNotNull(body);
                assertTrue(body.contains("order"));
                assertTrue(body.contains("Цена за единицу:"));
                assertTrue(body.contains("Количество:"));
                assertTrue(body.contains("Итого:"));
                assertTrue(body.contains("Общая сумма:"));
                assertTrue(body.contains(totalPrice.toString()));
            });
    }
}
