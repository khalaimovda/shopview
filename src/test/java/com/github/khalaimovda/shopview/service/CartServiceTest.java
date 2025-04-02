package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.OrderWithProducts;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.NoSuchElementException;

import static com.github.khalaimovda.shopview.utils.OrderUtils.*;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        OrderDetail cartDetail = getOrderDetail(generateRandomOrderWithProducts());
        Order cart = generateRandomActiveOrder();
        cart.setId(cartDetail.getOderId());
        when(orderRepository.findByIsActiveTrue()).thenReturn(Mono.just(cart));
        when(orderService.getOrderDetail(cart.getId())).thenReturn(Mono.just(cartDetail));

        // Act
        Mono<OrderDetail> monoOrderDetail = cartService.getCart();

        // Assert
        StepVerifier
            .create(monoOrderDetail)
            .assertNext(orderDetail -> assertEquals(cartDetail, orderDetail))
            .verifyComplete();
    }

    @Test
    void testAddProductToCartProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Mono.empty());
        StepVerifier
            .create(cartService.addProductToCart(136L))
            .expectError(NoSuchElementException.class)
            .verify();
        verify(productRepository, times(1)).findById(136L);
    }

    @Test
    void testAddProductToCartCartNotExist() {
        // Arrange
        Product product = generateRandomProduct();
        Order newCart = generateRandomActiveOrder();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Mono.empty());
        when(orderRepository.save(any())).thenReturn(Mono.just(newCart));
        when(orderProductService.addProductToOrder(anyLong(), anyLong())).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.addProductToCart(product.getId()))
            .verifyComplete();

        // Assert
        verify(productRepository, times(1)).findById(product.getId());

        // If current cart does not exist we must save the new one
        verify(orderRepository, times(1)).save(any(Order.class));

        verify(orderProductService, times(1)).addProductToOrder(newCart.getId(), product.getId());
    }

    @Test
    void testAddProductToCart() {
        // Arrange
        Product product = generateRandomProduct();
        Order cart = generateRandomActiveOrder();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Mono.just(cart));
        when(orderProductService.addProductToOrder(anyLong(), anyLong())).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.addProductToCart(product.getId()))
            .verifyComplete();

        // Assert
        verify(productRepository, times(1)).findById(product.getId());

        verify(orderRepository, times(1)).findByIsActiveTrue();

        // If current cart exists we must not save this cart, only order_product
        verify(orderRepository, never()).save(any());

        verify(orderProductService, times(1)).addProductToOrder(cart.getId(), product.getId());
    }

    @Test
    void testDecreaseProductInCartProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Mono.empty());
        StepVerifier
            .create(cartService.decreaseProductInCart(136L))
            .expectError(NoSuchElementException.class)
            .verify();
        verify(productRepository, times(1)).findById(136L);
    }

    @Test
    void testDecreaseProductInCartCartNotExist() {
        // Arrange
        Product product = generateRandomProduct();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.decreaseProductInCart(product.getId()))
            .expectError(NoSuchElementException.class)
            .verify();

        // Assert
        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testDecreaseProductInCart() {
        // Arrange
        Product product = generateRandomProduct();
        Order cart = generateRandomActiveOrder();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Mono.just(cart));
        when(orderProductService.decreaseProductInOrder(anyLong(), anyLong())).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.decreaseProductInCart(product.getId()))
            .verifyComplete();

        // Assert
        verify(productRepository, times(1)).findById(product.getId());

        verify(orderRepository, times(1)).findByIsActiveTrue();

        // If current cart exists we must not save this cart, only order_product
        verify(orderRepository, never()).save(any());

        verify(orderProductService, times(1)).decreaseProductInOrder(cart.getId(), product.getId());
    }


    @Test
    void testRemoveProductFromCartProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Mono.empty());
        StepVerifier
            .create(cartService.removeProductFromCart(136L))
            .expectError(NoSuchElementException.class)
            .verify();
        verify(productRepository, times(1)).findById(136L);
    }

    @Test
    void testRemoveProductFromCartCartNotExist() {
        // Arrange
        Product product = generateRandomProduct();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.removeProductFromCart(product.getId()))
            .expectError(NoSuchElementException.class)
            .verify();

        // Assert
        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testRemoveProductFromCart() {
        // Arrange
        Product product = generateRandomProduct();
        Order cart = generateRandomActiveOrder();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Mono.just(cart));
        when(orderProductService.removeProductFromOrder(anyLong(), anyLong())).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.removeProductFromCart(product.getId()))
            .verifyComplete();

        // Assert
        verify(productRepository, times(1)).findById(product.getId());

        verify(orderRepository, times(1)).findByIsActiveTrue();

        // If current cart exists we must not save this cart, only order_product
        verify(orderRepository, never()).save(any());

        verify(orderProductService, times(1)).removeProductFromOrder(cart.getId(), product.getId());
    }

    @Test
    void testCheckoutCartNotExist() {
        // Arrange
        when(orderRepository.findByIsActiveTrue()).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.checkout())
            .expectError(NoSuchElementException.class)
            .verify();

        // Assert
        verify(orderRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testCheckoutCartIsEmpty() {
        // Arrange

        // Empty cart
        OrderWithProducts orderWithProducts = generateRandomOrderWithProducts();
        orderWithProducts.setProducts(List.of());
        OrderDetail orderDetail = getOrderDetail(orderWithProducts);
        Order cart = generateRandomActiveOrder();
        cart.setId(orderWithProducts.getId());

        when(orderRepository.findByIsActiveTrue()).thenReturn(Mono.just(cart));
        when(orderService.getOrderDetail(cart.getId())).thenReturn(Mono.just(orderDetail));

        // Act and Assert
        StepVerifier
            .create(cartService.checkout())
            .expectError(IllegalStateException.class)
            .verify();

        verify(orderRepository, times(1)).findByIsActiveTrue();
        verify(orderService, times(1)).getOrderDetail(cart.getId());
    }

    @Test
    void testCheckoutCart() {
        // Arrange
        OrderWithProducts orderWithProducts = generateRandomOrderWithProducts();
        OrderDetail orderDetail = getOrderDetail(orderWithProducts);
        Order cart = generateRandomActiveOrder();
        cart.setId(orderWithProducts.getId());

        when(orderRepository.findByIsActiveTrue()).thenReturn(Mono.just(cart));
        when(orderService.getOrderDetail(cart.getId())).thenReturn(Mono.just(orderDetail));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(new Order()));

        // Act
        StepVerifier
            .create(cartService.checkout())
            .verifyComplete();

        // Assert
        verify(orderRepository, times(1)).findByIsActiveTrue();
        verify(orderService, times(1)).getOrderDetail(cart.getId());
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        cart.setIsActive(false);
        assertEquals(cart, orderCaptor.getValue());
    }
}
