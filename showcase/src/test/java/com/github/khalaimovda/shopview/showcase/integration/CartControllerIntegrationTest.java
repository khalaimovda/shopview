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
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.github.khalaimovda.shopview.showcase.utils.OrderUtils.calculateOrderPrice;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class CartControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderProductRepository orderProductRepository;

    @MockitoSpyBean
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void testGetCart() {
        Authentication auth = createAuthentication(ordinaryUser);

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
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
    void testGetCartAnonymousShouldBeRedirectedToLogin() {
        webTestClient
            .get()
            .uri("/cart")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    void testGetCartCached() throws InterruptedException {
        Authentication auth = createAuthentication(ordinaryUser);

        for (int i = 0; i < 15; i++) {
            webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
                .get()
                .uri("/cart")
                .exchange()
                .expectStatus().isOk();
        }
        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());

        // Wait until TTL is expired
        TimeUnit.SECONDS.sleep(2L);

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(2)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());
    }

    @Test
    void testAddNewProductToCart() {
        Authentication auth = createAuthentication(ordinaryUser);
        long productId = 1L;

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/cart/add/" + productId)
            .exchange()
            .expectStatus().isOk();

        Order cart = orderRepository.findByUserIdAndIsActiveTrue(ordinaryUser.getId()).block();
        Product product = productRepository.findById(productId).block();
        OrderProduct orderProduct = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block();

        assertEquals(1, orderProduct.getCount());
        assertEquals(productId, orderProduct.getProductId());
        assertEquals(cart.getId(), orderProduct.getOrderId());
    }

    @Test
    void testAddNewProductToCartAnonymousShouldBeRedirectedToLogin() {
        long productId = 1L;

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/cart/add/" + productId)
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    void testAddNewProductToCartForbiddenWithoutCsrf() {
        Authentication auth = createAuthentication(ordinaryUser);
        long productId = 1L;

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .post()
            .uri("/cart/add/" + productId)
            .exchange()
            .expectStatus().isForbidden();
    }


    @Test
    void testAddNewProductToCartInvalidCache() {
        Authentication auth = createAuthentication(ordinaryUser);

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());

        // Add new post into cart
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/cart/add/" + 1)
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(2)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());
        // After this cache must be invalidated

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(3)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());
    }
    @Test
    void testAddExistingProductToCart() {
        Authentication auth = createAuthentication(ordinaryUser);
        long productId = 13L;

        Order cart = orderRepository.findByUserIdAndIsActiveTrue(ordinaryUser.getId()).block();
        Product product = productRepository.findById(productId).block();
        int prevCount = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block().getCount();

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/cart/add/" + productId)
            .exchange()
            .expectStatus().isOk();

        int newCount = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block().getCount();
        assertEquals(prevCount + 1, newCount);
    }

    @Test
    void testDecreaseProductInCartCountMoreThanOne() {
        Authentication auth = createAuthentication(ordinaryUser);
        long productId = 13L;

        Order cart = orderRepository.findByUserIdAndIsActiveTrue(ordinaryUser.getId()).block();
        Product product = productRepository.findById(productId).block();
        int prevCount = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block().getCount();

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/cart/decrease/" + productId)
            .exchange()
            .expectStatus().isOk();

        int newCount = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block().getCount();
        assertEquals(prevCount - 1, newCount);
    }

    @Test
    void testDecreaseProductInCartCountOneThenRemove() {
        Authentication auth = createAuthentication(ordinaryUser);
        long productId = 13L;

        Order cart = orderRepository.findByUserIdAndIsActiveTrue(ordinaryUser.getId()).block();
        Product product = productRepository.findById(productId).block();

        OrderProduct orderProduct = orderProductRepository
            .findByOrderIdAndProductId(cart.getId(), product.getId()).block();
        orderProduct.setCount(1);
        orderProductRepository.save(orderProduct).block();

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/cart/decrease/" + productId)
            .exchange()
            .expectStatus().isOk();

        StepVerifier
            .create(orderProductRepository.findByOrderIdAndProductId(cart.getId(), product.getId()))
            .verifyComplete();
    }

    @Test
    void testDecreaseProductInCartInvalidCache() {
        Authentication auth = createAuthentication(ordinaryUser);
        long productId = 13L;

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/cart/decrease/" + productId)
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(2)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(3)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());
    }

    @Test
    void testRemoveProductFromCart() {
        Authentication auth = createAuthentication(ordinaryUser);
        long productId = 13L;

        Order cart = orderRepository.findByUserIdAndIsActiveTrue(ordinaryUser.getId()).block();
        Product product = productRepository.findById(productId).block();

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .delete()
            .uri("/cart/remove/" + productId)
            .exchange()
            .expectStatus().isOk();

        StepVerifier
            .create(orderProductRepository.findByOrderIdAndProductId(cart.getId(), product.getId()))
            .verifyComplete();
    }

    @Test
    void testRemoveProductFromCartInvalidCache() {
        Authentication auth = createAuthentication(ordinaryUser);
        long productId = 13L;

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .delete()
            .uri("/cart/remove/" + productId)
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(2)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(3)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());
    }

    @Test
    void testCheckout() {
        Authentication auth = createAuthentication(ordinaryUser);
        Order cart = orderRepository.findByUserIdAndIsActiveTrue(ordinaryUser.getId()).block();
        long orderId = cart.getId();
        OrderWithProducts orderWithProducts = orderRepository.findOrderWithProductsById(orderId).block();
        BigDecimal totalPrice = calculateOrderPrice(orderWithProducts);

        when(paymentService.makePayment(anyLong(), any(BigDecimal.class)))
            .thenReturn(Mono.just(new Balance().balance(BigDecimal.TWO)));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/cart/checkout")
            .exchange()
            .expectStatus().isOk();

        verify(paymentService, times(1)).makePayment(ordinaryUser.getId(), totalPrice);

        Order order = orderRepository.findById(orderId).block();
        assertFalse(order.getIsActive());
    }

    @Test
    void testCheckoutInvalidCache() {
        Authentication auth = createAuthentication(ordinaryUser);

        when(paymentService.makePayment(anyLong(), any(BigDecimal.class)))
            .thenReturn(Mono.just(new Balance().balance(BigDecimal.TWO)));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/cart/checkout")
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(2)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk();
        verify(orderRepository, times(3)).findByUserIdAndIsActiveTrue(ordinaryUser.getId());
    }

    @Test
    void testCheckoutInsufficientFunds() {
        Authentication auth = createAuthentication(ordinaryUser);

        Order cart = orderRepository.findByUserIdAndIsActiveTrue(ordinaryUser.getId()).block();
        long orderId = cart.getId();
        OrderWithProducts orderWithProducts = orderRepository.findOrderWithProductsById(orderId).block();
        BigDecimal totalPrice = calculateOrderPrice(orderWithProducts);
        BigDecimal availableBalance = totalPrice.subtract(BigDecimal.ONE);

        when(paymentService.makePayment(anyLong(), any(BigDecimal.class)))
            .thenReturn(Mono.error(new InsufficientFundsException(
                HttpStatus.BAD_REQUEST, "Not enough funds to complete transaction", totalPrice, availableBalance)));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/cart/checkout")
            .exchange()
            .expectStatus().isBadRequest();

        verify(paymentService, times(1)).makePayment(ordinaryUser.getId(), totalPrice);

        Order order = orderRepository.findById(orderId).block();
        assertTrue(order.getIsActive());
    }
}
