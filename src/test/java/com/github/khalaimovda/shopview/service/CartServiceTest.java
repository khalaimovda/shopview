package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.khalaimovda.shopview.utils.OrderUtils.generateRandomActiveOrder;
import static com.github.khalaimovda.shopview.utils.OrderUtils.getOrderDetail;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProduct;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderProductService orderProductService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CartServiceImpl cartService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Test
    void testGetCart() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order cart = generateRandomActiveOrder(products);
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.of(cart));
        OrderDetail expectedOrderDetail = getOrderDetail(cart);
        when(orderService.getOrderDetail(cart)).thenReturn(expectedOrderDetail);

        // Act
        Optional<OrderDetail> optionalOrderDetail = cartService.getCart();

        // Assert
        assertTrue(optionalOrderDetail.isPresent());
        OrderDetail orderDetail = optionalOrderDetail.get();
        assertEquals(expectedOrderDetail, orderDetail);
    }

    @Test
    void testAddProductToCartProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> cartService.addProductToCart(136L));
        verify(productRepository, times(1)).findById(136L);
    }

    @Test
    void testAddProductToCartCartNotExist() {
        // Arrange
        Product product = generateRandomProduct();
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.empty());
        when(orderRepository.save(any())).thenReturn(new Order());

        // Act
        cartService.addProductToCart(product.getId());

        // Assert
        verify(productRepository, times(1)).findById(product.getId());

        // If current cart does not exist we must save the new one
        verify(orderRepository, times(1)).save(any());

        verify(orderProductService, times(1)).addProductToOrder(orderCaptor.capture(), productCaptor.capture());

        assertTrue(orderCaptor.getValue().getOrderProducts().isEmpty());
        assertEquals(product, productCaptor.getValue());
    }

    @Test
    void testAddProductToCart() {
        // Arrange
        Product product = generateRandomProduct();
        List<Product> products = generateRandomProducts(5);
        Order cart = generateRandomActiveOrder(products);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.of(cart));

        // Act
        cartService.addProductToCart(product.getId());

        // Assert
        verify(productRepository, times(1)).findById(product.getId());

        // If current cart exists we must not save this cart, only order_product
        verify(orderRepository, never()).save(any());

        verify(orderProductService, times(1)).addProductToOrder(orderCaptor.capture(), productCaptor.capture());

        assertEquals(cart, orderCaptor.getValue());
        assertEquals(product, productCaptor.getValue());
    }

    @Test
    void testDecreaseProductInCartProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> cartService.decreaseProductInCart(136L));
        verify(productRepository, times(1)).findById(136L);
    }

    @Test
    void testDecreaseProductInCartCartNotExist() {
        // Arrange
        Product product = generateRandomProduct();
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(NoSuchElementException.class, () -> cartService.decreaseProductInCart(product.getId()));
        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testDecreaseProductInCart() {
        // Arrange
        Product product = generateRandomProduct();
        List<Product> products = generateRandomProducts(5);
        Order cart = generateRandomActiveOrder(products);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.of(cart));

        // Act
        cartService.decreaseProductInCart(product.getId());

        // Assert
        verify(productRepository, times(1)).findById(product.getId());

        // If current cart exists we must not save this cart, only order_product
        verify(orderRepository, never()).save(any());

        verify(orderProductService, times(1)).decreaseProductInOrder(orderCaptor.capture(), productCaptor.capture());

        assertEquals(cart, orderCaptor.getValue());
        assertEquals(product, productCaptor.getValue());
    }


    @Test
    void testRemoveProductFromCartProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> cartService.removeProductFromCart(136L));
        verify(productRepository, times(1)).findById(136L);
    }

    @Test
    void testRemoveProductFromCartCartNotExist() {
        // Arrange
        Product product = generateRandomProduct();
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(NoSuchElementException.class, () -> cartService.removeProductFromCart(product.getId()));
        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testRemoveProductFromCart() {
        // Arrange
        Product product = generateRandomProduct();
        List<Product> products = generateRandomProducts(5);
        Order cart = generateRandomActiveOrder(products);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.of(cart));

        // Act
        cartService.removeProductFromCart(product.getId());

        // Assert
        verify(productRepository, times(1)).findById(product.getId());

        // If current cart exists we must not save this cart, only order_product
        verify(orderRepository, never()).save(any());

        verify(orderProductService, times(1)).removeProductFromOrder(orderCaptor.capture(), productCaptor.capture());

        assertEquals(cart, orderCaptor.getValue());
        assertEquals(product, productCaptor.getValue());
    }

    @Test
    void testCheckoutCartNotExist() {
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> cartService.checkout());
        verify(orderRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testCheckoutCartIsEmpty() {
        // Arrange
        Order cart = generateRandomActiveOrder(List.of()); // Empty cart
        OrderDetail orderDetail = getOrderDetail(cart);
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.of(cart));
        when(orderService.getOrderDetail(cart)).thenReturn(orderDetail);

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> cartService.checkout());
        verify(orderRepository, times(1)).findByIsActiveTrue();
        verify(orderService, times(1)).getOrderDetail(cart);
    }

    @Test
    void testCheckoutCart() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order cart = generateRandomActiveOrder(products);
        OrderDetail orderDetail = getOrderDetail(cart);
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.of(cart));
        when(orderService.getOrderDetail(cart)).thenReturn(orderDetail);

        // Act
        cartService.checkout();

        // Assert
        verify(orderRepository, times(1)).findByIsActiveTrue();
        verify(orderService, times(1)).getOrderDetail(cart);
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        cart.setIsActive(false);
        assertEquals(cart, orderCaptor.getValue());
    }
}
