package com.github.khalaimovda.shopview.showcase.integration;

import com.github.khalaimovda.shopview.showcase.domain.Balance;
import com.github.khalaimovda.shopview.showcase.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.showcase.exception.InsufficientFundsException;
import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.OrderProduct;
import com.github.khalaimovda.shopview.showcase.model.Product;
import com.github.khalaimovda.shopview.showcase.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import com.github.khalaimovda.shopview.showcase.repository.ProductRepository;
import com.github.khalaimovda.shopview.showcase.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.github.khalaimovda.shopview.showcase.utils.OrderUtils.calculateOrderPrice;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class CartControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void testGetCart() throws Exception {
        webTestClient.get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_HTML)
            .expectBody(String.class)
            .consumeWith(response -> {
                String body = response.getResponseBody();
                assertNotNull(body);

                // Check that the model contains the "cart" attribute
                assertTrue(body.contains("cart"));
                // Verify that the cart page is not empty and contains a specific product
                assertTrue(body.contains("Товар 13"));

                // Check for the unit price label and specific unit price (e.g., product 13: 0.99 + 13 = 13.99)
                assertTrue(body.contains("Цена за единицу:"));
                assertTrue(body.contains("13.99"));

                // Check that the product count is displayed correctly (e.g., for order 10, count = 10)
                assertTrue(body.contains("<span class=\"product-count-regulation-value\">10</span>"));

                // Verify that the total price for the product is calculated and displayed (e.g., 10 * 13.99 = 139.9)
                assertTrue(body.contains("139.9"));

                // Verify that the "Оформить заказ" (Place Order) button is present on the page
                assertTrue(body.contains("Оформить заказ"));

            });
    }

    @Test
    void testAddNewProductToCart() throws Exception {
        long productId = 1L;

        webTestClient.post()
            .uri("/cart/add/" + productId)
            .exchange()
            .expectStatus().isOk();

        Order cart = orderRepository.findByIsActiveTrue().block();
        Product product = productRepository.findById(productId).block();
        OrderProduct orderProduct = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block();

        assertEquals(1, orderProduct.getCount());
        assertEquals(productId, orderProduct.getProductId());
        assertEquals(cart.getId(), orderProduct.getOrderId());
    }

    @Test
    void testAddExistingProductToCart() throws Exception {
        long productId = 13L;
        Order cart = orderRepository.findByIsActiveTrue().block();
        Product product = productRepository.findById(productId).block();
        int prevCount = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block().getCount();

        webTestClient.post()
            .uri("/cart/add/" + productId)
            .exchange()
            .expectStatus().isOk();

        int newCount = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block().getCount();
        assertEquals(prevCount + 1, newCount);
    }

    @Test
    void testDecreaseProductInCartCountMoreThanOne() throws Exception {
        long productId = 13L;
        Order cart = orderRepository.findByIsActiveTrue().block();
        Product product = productRepository.findById(productId).block();
        int prevCount = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block().getCount();

        webTestClient.post()
            .uri("/cart/decrease/" + productId)
            .exchange()
            .expectStatus().isOk();

        int newCount = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block().getCount();
        assertEquals(prevCount - 1, newCount);
    }

    @Test
    void testDecreaseProductInCartCountOneThenRemove() throws Exception {
        long productId = 13L;
        Order cart = orderRepository.findByIsActiveTrue().block();
        Product product = productRepository.findById(productId).block();

        // Set count to 1
        OrderProduct orderProduct = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block();
        orderProduct.setCount(1);
        orderProductRepository.save(orderProduct).block();

        webTestClient.post()
            .uri("/cart/decrease/" + productId)
            .exchange()
            .expectStatus().isOk();

        // This product must be removed from cart
        StepVerifier
            .create(orderProductRepository.findByOrderIdAndProductId(cart.getId(), product.getId()))
            .verifyComplete();
    }

    @Test
    void testRemoveProductFromCart() throws Exception {
        long productId = 13L;
        Order cart = orderRepository.findByIsActiveTrue().block();
        Product product = productRepository.findById(productId).block();

        webTestClient.delete()
            .uri("/cart/remove/" + productId)
            .exchange()
            .expectStatus().isOk();

        // This product must be removed from cart
        StepVerifier
            .create(orderProductRepository.findByOrderIdAndProductId(cart.getId(), product.getId()))
            .verifyComplete();
    }

    @Test
    void testCheckout() throws Exception {
        Order cart = orderRepository.findByIsActiveTrue().block();
        long orderId = cart.getId();
        OrderWithProducts orderWithProducts = orderRepository.findOrderWithProductsById(orderId).block();
        BigDecimal totalPrice = calculateOrderPrice(orderWithProducts);

        when(paymentService.makePayment(any(BigDecimal.class)))
            .thenReturn(Mono.just(new Balance().balance(BigDecimal.TWO)));

        webTestClient.post()
            .uri("/cart/checkout")
            .exchange()
            .expectStatus().isOk();

        verify(paymentService, times(1)).makePayment(totalPrice);

        Order order = orderRepository.findById(orderId).block();
        assertFalse(order.getIsActive());
    }

    @Test
    void testCheckoutInsufficientFunds() throws Exception {
        Order cart = orderRepository.findByIsActiveTrue().block();
        long orderId = cart.getId();
        OrderWithProducts orderWithProducts = orderRepository.findOrderWithProductsById(orderId).block();
        BigDecimal totalPrice = calculateOrderPrice(orderWithProducts);
        BigDecimal availableBalance = totalPrice.subtract(BigDecimal.ONE);

        when(paymentService.makePayment(any(BigDecimal.class)))
            .thenReturn(Mono.just(new Balance().balance(BigDecimal.TWO)));

        when(paymentService.makePayment(any(BigDecimal.class))).thenReturn(Mono.error(new InsufficientFundsException(
            HttpStatus.BAD_REQUEST, "Not enough funds to complete transaction", totalPrice, availableBalance)));

        webTestClient.post()
            .uri("/cart/checkout")
            .exchange()
            .expectStatus().isBadRequest();

        verify(paymentService, times(1)).makePayment(totalPrice);

        Order order = orderRepository.findById(orderId).block();
        assertTrue(order.getIsActive());
    }
}
