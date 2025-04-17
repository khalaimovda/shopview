package com.github.khalaimovda.shopview.showcase.controller;


import com.github.khalaimovda.shopview.showcase.dto.OrderDetail;
import com.github.khalaimovda.shopview.showcase.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.Product;
import com.github.khalaimovda.shopview.showcase.security.AuthenticatedUser;
import com.github.khalaimovda.shopview.showcase.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.github.khalaimovda.shopview.showcase.utils.OrderUtils.*;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProducts;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @InjectMocks
    private CartController cartController;

    private final AuthenticatedUser authenticatedUser = new AuthenticatedUser(
        42L, "testuser", "encodedPassword", List.of()
    );

    @Test
    void testGetCart() {
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder();
        OrderWithProducts orderWithProducts = generateRandomOrderWithProducts(order.getId(), products);
        OrderDetail cart = getOrderDetail(orderWithProducts);
        when(cartService.getCart(anyLong())).thenReturn(Mono.just(cart));

        StepVerifier
            .create(cartController.getCart(authenticatedUser, model))
            .expectNext("cart")
            .verifyComplete();

        verify(cartService, times(1)).getCart(authenticatedUser.getId());
        verify(model).addAttribute("cart", cart);
    }

    @Test
    void testAddProductToCart() throws Exception {
        long productId = 13L;
        when(cartService.addProductToCart(anyLong(), anyLong())).thenReturn(Mono.empty());

        StepVerifier
            .create(cartController.addProductToCart(authenticatedUser, productId))
            .verifyComplete();

        verify(cartService, times(1)).addProductToCart(productId, authenticatedUser.getId());
    }

    @Test
    void testDecreaseProductInCart() throws Exception {
        long productId = 13L;
        when(cartService.decreaseProductInCart(anyLong(), anyLong())).thenReturn(Mono.empty());

        StepVerifier
            .create(cartController.decreaseProductInCart(authenticatedUser, productId))
            .verifyComplete();

        verify(cartService, times(1)).decreaseProductInCart(productId, authenticatedUser.getId());
    }

    @Test
    void testRemoveProductFromCart() throws Exception {
        long productId = 13L;
        when(cartService.removeProductFromCart(anyLong(), anyLong())).thenReturn(Mono.empty());

        StepVerifier
            .create(cartController.removeProductFromCart(authenticatedUser, productId))
            .verifyComplete();

        verify(cartService, times(1)).removeProductFromCart(productId, authenticatedUser.getId());
    }

    @Test
    void testCheckout() throws Exception {
        when(cartService.checkout(anyLong())).thenReturn(Mono.empty());

        StepVerifier
            .create(cartController.checkout(authenticatedUser))
            .verifyComplete();

        verify(cartService, times(1)).checkout(authenticatedUser.getId());
    }
}
