package com.github.khalaimovda.shopview.controller;


import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static com.github.khalaimovda.shopview.utils.OrderUtils.generateRandomActiveOrder;
import static com.github.khalaimovda.shopview.utils.OrderUtils.getOrderDetail;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProducts;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void testGetCart() throws Exception {
        OrderDetail orderDetail =  getOrderDetail(generateRandomActiveOrder(generateRandomProducts(5)));
        when(cartService.getCart()).thenReturn(Optional.of(orderDetail));

        mockMvc.perform(get("/cart"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("cart"));

        verify(cartService, times(1)).getCart();
    }

    @Test
    void testAddProductToCart() throws Exception {
        long productId = 13L;

        mockMvc.perform(post("/cart/add/" + productId))
            .andExpect(status().isOk());

        verify(cartService, times(1)).addProductToCart(productId);
    }

    @Test
    void testDecreaseProductInCart() throws Exception {
        long productId = 13L;

        mockMvc.perform(post("/cart/decrease/" + productId))
            .andExpect(status().isOk());

        verify(cartService, times(1)).decreaseProductInCart(productId);
    }

    @Test
    void testRemoveProductFromCart() throws Exception {
        long productId = 13L;

        mockMvc.perform(delete("/cart/remove/" + productId))
            .andExpect(status().isOk());

        verify(cartService, times(1)).removeProductFromCart(productId);
    }

    @Test
    void testCheckout() throws Exception {
        mockMvc.perform(post("/cart/checkout"))
            .andExpect(status().isOk());

        verify(cartService, times(1)).checkout();
    }
}
