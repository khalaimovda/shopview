package com.github.khalaimovda.shopview.controller;


import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.github.khalaimovda.shopview.utils.OrderUtils.*;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


@WebFluxTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CartService cartService;

    @Test
    void testGetCart() throws Exception {
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder();
        OrderWithProducts orderWithProducts = generateRandomOrderWithProducts(order.getId(), products);
        OrderDetail orderDetail = getOrderDetail(orderWithProducts);
        when(cartService.getCart()).thenReturn(Mono.just(orderDetail));

        webTestClient
            .get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_HTML)
            .expectBody(String.class)
            .consumeWith(response -> {
               String body = response.getResponseBody();
               assertNotNull(body);
               assertTrue(body.contains("cart"));
            });

        verify(cartService, times(1)).getCart();
    }

    @Test
    void testAddProductToCart() throws Exception {
        long productId = 13L;
        when(cartService.addProductToCart(anyLong())).thenReturn(Mono.empty());

        webTestClient
            .post()
            .uri("/cart/add/" + productId)
            .exchange()
            .expectStatus().isOk();

        verify(cartService, times(1)).addProductToCart(productId);
    }

    @Test
    void testDecreaseProductInCart() throws Exception {
        long productId = 13L;
        when(cartService.decreaseProductInCart(anyLong())).thenReturn(Mono.empty());

        webTestClient
            .post()
            .uri("/cart/decrease/" + productId)
            .exchange()
            .expectStatus().isOk();

        verify(cartService, times(1)).decreaseProductInCart(productId);
    }

    @Test
    void testRemoveProductFromCart() throws Exception {
        long productId = 13L;
        when(cartService.removeProductFromCart(anyLong())).thenReturn(Mono.empty());

        webTestClient
            .delete()
            .uri("/cart/remove/" + productId)
            .exchange()
            .expectStatus().isOk();

        verify(cartService, times(1)).removeProductFromCart(productId);
    }

    @Test
    void testCheckout() throws Exception {
        when(cartService.checkout()).thenReturn(Mono.empty());

        webTestClient
            .post()
            .uri("/cart/checkout")
            .exchange()
            .expectStatus().isOk();

        verify(cartService, times(1)).checkout();
    }
}
