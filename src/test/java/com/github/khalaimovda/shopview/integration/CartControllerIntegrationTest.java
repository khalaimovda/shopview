package com.github.khalaimovda.shopview.integration;

import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
public class CartControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testGetCart() throws Exception {
        mockMvc.perform(get("/cart"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("cart"))
            // Check that the model contains the "cart" attribute
            .andExpect(model().attributeExists("cart"))
            // Verify that the cart page is not empty and contains a specific product
            .andExpect(content().string(containsString("Товар 13")))
            // Check for the unit price label and specific unit price (e.g., product 13: 0.99 + 13 = 13.99)
            .andExpect(content().string(containsString("Цена за единицу:")))
            .andExpect(content().string(containsString("13.99")))
            // Check that the product count is displayed correctly (e.g., for order 10, count = 10)
            .andExpect(content().string(containsString("<span class=\"product-count-regulation-value\">10</span>")))
            // Verify that the total price for the product is calculated and displayed (e.g., 10 * 13.99 = 139.9)
            .andExpect(content().string(containsString("139.9")))
            // Verify that the "Оформить заказ" (Place Order) button is present on the page
            .andExpect(content().string(containsString("Оформить заказ")));
    }

    @Test
    void testAddNewProductToCart() throws Exception {
        long productId = 1L;

        mockMvc.perform(post("/cart/add/" + productId))
            .andExpect(status().isOk());

        Order cart = orderRepository.findByIsActiveTrue().get();
        Product product = productRepository.findById(productId).get();
        Optional<OrderProduct> optionalOrderProduct = orderProductRepository.findByOrderAndProduct(cart, product);

        assertTrue(optionalOrderProduct.isPresent());
        OrderProduct orderProduct = optionalOrderProduct.get();
        assertEquals(1, orderProduct.getCount());
        Product addedProduct = orderProduct.getProduct();
        assertAll(
            () -> assertEquals(product.getId(), addedProduct.getId()),
            () -> assertEquals(product.getName(), addedProduct.getName()),
            () -> assertEquals(product.getDescription(), addedProduct.getDescription()),
            () -> assertEquals(product.getImagePath(), addedProduct.getImagePath()),
            () -> assertEquals(product.getPrice(), addedProduct.getPrice())
        );
    }

    @Test
    void testAddExistingProductToCart() throws Exception {
        long productId = 13L;
        Order cart = orderRepository.findByIsActiveTrue().get();
        Product product = productRepository.findById(productId).get();
        int prevCount = orderProductRepository.findByOrderAndProduct(cart, product).get().getCount();

        mockMvc.perform(post("/cart/add/" + productId))
            .andExpect(status().isOk());

        int newCount = orderProductRepository.findByOrderAndProduct(cart, product).get().getCount();
        assertEquals(prevCount + 1, newCount);
    }

    @Test
    void testDecreaseProductInCartCountMoreThanOne() throws Exception {
        long productId = 13L;
        Order cart = orderRepository.findByIsActiveTrue().get();
        Product product = productRepository.findById(productId).get();
        int prevCount = orderProductRepository.findByOrderAndProduct(cart, product).get().getCount();

        mockMvc.perform(post("/cart/decrease/" + productId))
            .andExpect(status().isOk());

        int newCount = orderProductRepository.findByOrderAndProduct(cart, product).get().getCount();
        assertEquals(prevCount - 1, newCount);
    }

    @Test
    void testDecreaseProductInCartCountOneThenRemove() throws Exception {
        long productId = 13L;
        Order cart = orderRepository.findByIsActiveTrue().get();
        Product product = productRepository.findById(productId).get();

        // Set count to 1
        OrderProduct orderProduct = orderProductRepository.findByOrderAndProduct(cart, product).get();
        orderProduct.setCount(1);
        orderProductRepository.save(orderProduct);

        mockMvc.perform(post("/cart/decrease/" + productId))
            .andExpect(status().isOk());

        // This product must be removed from cart
        Optional<OrderProduct> optionalOrderProduct = orderProductRepository.findByOrderAndProduct(cart, product);
        assertTrue(optionalOrderProduct.isEmpty());
    }

    @Test
    void testRemoveProductFromCart() throws Exception {
        long productId = 13L;
        Order cart = orderRepository.findByIsActiveTrue().get();
        Product product = productRepository.findById(productId).get();

        mockMvc.perform(delete("/cart/remove/" + productId))
            .andExpect(status().isOk());

        // This product must be removed from cart
        Optional<OrderProduct> optionalOrderProduct = orderProductRepository.findByOrderAndProduct(cart, product);
        assertTrue(optionalOrderProduct.isEmpty());
    }

    @Test
    void testCheckout() throws Exception {
        Order cart = orderRepository.findByIsActiveTrue().get();
        long orderId = cart.getId();

        mockMvc.perform(post("/cart/checkout"))
            .andExpect(status().isOk());

        Order order = orderRepository.findById(orderId).get();
        assertFalse(order.getIsActive());
    }
}
