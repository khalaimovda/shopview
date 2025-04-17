package com.github.khalaimovda.shopview.showcase.integration;


import com.github.khalaimovda.shopview.showcase.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.showcase.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import com.github.khalaimovda.shopview.showcase.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.github.khalaimovda.shopview.showcase.utils.OrderUtils.calculateOrderPrice;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @MockitoSpyBean
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testGetAllOrders() {
        Long orderId = orderRepository.findById(1L).block().getId();
        OrderWithProducts order = orderRepository.findOrderWithProductsById(orderId).block();
        BigDecimal totalPrice = calculateOrderPrice(order);
        Authentication auth = createAuthentication(ordinaryUser);

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
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
                assertFalse(body.contains("Нет оформленных заказов"));
                assertTrue(body.contains("Заказ #1"));
                assertTrue(body.contains(totalPrice + " ₽"));
            });
    }

    @Test
    void testGetAllOrdersAnonymousShouldBeRedirectedToLogin() {
        webTestClient
            .get()
            .uri("/orders")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    void testGetAllOrdersCached() throws Exception {
        Authentication auth = createAuthentication(ordinaryUser);
        for (int i = 0; i < 15; i++) {
            webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
                .get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
        }
        verify(orderRepository, times(1)).findAllPlacedOrdersWithProducts(ordinaryUser.getId());

        // Wait until TTL is expired
        TimeUnit.SECONDS.sleep(2L);

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/orders")
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(2)).findAllPlacedOrdersWithProducts(ordinaryUser.getId());
    }

    @Test
    void testGetOrderById() {
        Long orderId = orderRepository.findById(1L).block().getId();
        OrderWithProducts order = orderRepository.findOrderWithProductsById(orderId).block();
        BigDecimal totalPrice = calculateOrderPrice(order);
        Authentication auth = createAuthentication(ordinaryUser);

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
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

    @Test
    void testGetOrderByIdAnonymousShouldBeRedirectedToLogin() {
        webTestClient
            .get()
            .uri("/orders/" + 1L)
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    void testGetOrderByIdCached() throws Exception {
        Long orderId = 1L;
        Authentication auth = createAuthentication(ordinaryUser);

        for (int i = 0; i < 15; i++) {
            webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
                .get()
                .uri("/orders/" + orderId)
                .exchange()
                .expectStatus().isOk();
        }
        verify(orderRepository, times(1)).findOrderWithProductsById(orderId);

        // Wait until TTL is expired
        TimeUnit.SECONDS.sleep(2L);

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/orders/" + orderId)
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(2)).findOrderWithProductsById(orderId);
    }
}
