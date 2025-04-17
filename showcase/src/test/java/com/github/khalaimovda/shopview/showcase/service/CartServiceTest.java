package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.domain.Balance;
import com.github.khalaimovda.shopview.showcase.dto.OrderDetail;
import com.github.khalaimovda.shopview.showcase.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.showcase.exception.InsufficientFundsException;
import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.Product;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import com.github.khalaimovda.shopview.showcase.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static com.github.khalaimovda.shopview.showcase.utils.OrderUtils.*;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProduct;
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

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private CartServiceImpl cartService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Test
    void testGetCart() {
        // Arrange
        long userId = 13L;
        OrderDetail cartDetail = getOrderDetail(generateRandomOrderWithProducts());
        Order cart = generateRandomActiveOrder();
        cart.setId(cartDetail.getOderId());
        when(orderRepository.findByUserIdAndIsActiveTrue(anyLong())).thenReturn(Mono.just(cart));
        when(orderService.getOrderDetail(cart.getId())).thenReturn(Mono.just(cartDetail));

        // Act
        Mono<OrderDetail> monoOrderDetail = cartService.getCart(userId);

        // Assert
        StepVerifier
            .create(monoOrderDetail)
            .assertNext(orderDetail -> assertEquals(cartDetail, orderDetail))
            .verifyComplete();
    }

    @Test
    void testAddProductToCartProductNotFound() {
        long userId = 13L;
        when(productRepository.findById(anyLong())).thenReturn(Mono.empty());
        StepVerifier
            .create(cartService.addProductToCart(136L, userId))
            .expectError(NoSuchElementException.class)
            .verify();
        verify(productRepository, times(1)).findById(136L);
    }

    @Test
    void testAddProductToCartCartNotExist() {
        // Arrange
        long userId = 13L;
        Product product = generateRandomProduct();
        Order newCart = generateRandomActiveOrder();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByUserIdAndIsActiveTrue(anyLong())).thenReturn(Mono.empty());
        when(orderRepository.save(any())).thenReturn(Mono.just(newCart));
        when(orderProductService.addProductToOrder(anyLong(), anyLong())).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.addProductToCart(product.getId(), userId))
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
        long userId = 13L;
        Product product = generateRandomProduct();
        Order cart = generateRandomActiveOrder();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByUserIdAndIsActiveTrue(anyLong())).thenReturn(Mono.just(cart));
        when(orderProductService.addProductToOrder(anyLong(), anyLong())).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.addProductToCart(product.getId(), userId))
            .verifyComplete();

        // Assert
        verify(productRepository, times(1)).findById(product.getId());

        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(userId);

        // If current cart exists we must not save this cart, only order_product
        verify(orderRepository, never()).save(any());

        verify(orderProductService, times(1)).addProductToOrder(cart.getId(), product.getId());
    }

    @Test
    void testDecreaseProductInCartProductNotFound() {
        long userId = 13L;
        when(productRepository.findById(anyLong())).thenReturn(Mono.empty());
        StepVerifier
            .create(cartService.decreaseProductInCart(136L, userId))
            .expectError(NoSuchElementException.class)
            .verify();
        verify(productRepository, times(1)).findById(136L);
    }

    @Test
    void testDecreaseProductInCartCartNotExist() {
        // Arrange
        long userId = 13L;
        Product product = generateRandomProduct();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByUserIdAndIsActiveTrue(anyLong())).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.decreaseProductInCart(product.getId(), userId))
            .expectError(NoSuchElementException.class)
            .verify();

        // Assert
        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(userId);
    }

    @Test
    void testDecreaseProductInCart() {
        // Arrange
        long userId = 13L;
        Product product = generateRandomProduct();
        Order cart = generateRandomActiveOrder();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByUserIdAndIsActiveTrue(userId)).thenReturn(Mono.just(cart));
        when(orderProductService.decreaseProductInOrder(anyLong(), anyLong())).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.decreaseProductInCart(product.getId(), userId))
            .verifyComplete();

        // Assert
        verify(productRepository, times(1)).findById(product.getId());

        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(userId);

        // If current cart exists we must not save this cart, only order_product
        verify(orderRepository, never()).save(any());

        verify(orderProductService, times(1)).decreaseProductInOrder(cart.getId(), product.getId());
    }


    @Test
    void testRemoveProductFromCartProductNotFound() {
        long userId = 13L;
        when(productRepository.findById(anyLong())).thenReturn(Mono.empty());
        StepVerifier
            .create(cartService.removeProductFromCart(136L, userId))
            .expectError(NoSuchElementException.class)
            .verify();
        verify(productRepository, times(1)).findById(136L);
    }

    @Test
    void testRemoveProductFromCartCartNotExist() {
        // Arrange
        long userId = 13L;
        Product product = generateRandomProduct();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByUserIdAndIsActiveTrue(anyLong())).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.removeProductFromCart(product.getId(), userId))
            .expectError(NoSuchElementException.class)
            .verify();

        // Assert
        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(userId);
    }

    @Test
    void testRemoveProductFromCart() {
        // Arrange
        long userId = 13L;
        Product product = generateRandomProduct();
        Order cart = generateRandomActiveOrder();
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(orderRepository.findByUserIdAndIsActiveTrue(anyLong())).thenReturn(Mono.just(cart));
        when(orderProductService.removeProductFromOrder(anyLong(), anyLong())).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.removeProductFromCart(product.getId(), userId))
            .verifyComplete();

        // Assert
        verify(productRepository, times(1)).findById(product.getId());

        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(userId);

        // If current cart exists we must not save this cart, only order_product
        verify(orderRepository, never()).save(any());

        verify(orderProductService, times(1)).removeProductFromOrder(cart.getId(), product.getId());
    }

    @Test
    void testCheckoutCartNotExist() {
        // Arrange
        long userId = 13L;
        when(orderRepository.findByUserIdAndIsActiveTrue(anyLong())).thenReturn(Mono.empty());

        // Act
        StepVerifier
            .create(cartService.checkout(userId))
            .expectError(NoSuchElementException.class)
            .verify();

        // Assert
        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(userId);
    }

    @Test
    void testCheckoutCartIsEmpty() {
        // Arrange
        long userId = 13L;

        // Empty cart
        OrderWithProducts orderWithProducts = generateRandomOrderWithProducts();
        orderWithProducts.setProducts(List.of());
        OrderDetail orderDetail = getOrderDetail(orderWithProducts);
        Order cart = generateRandomActiveOrder();
        cart.setId(orderWithProducts.getId());

        when(orderRepository.findByUserIdAndIsActiveTrue(anyLong())).thenReturn(Mono.just(cart));
        when(orderService.getOrderDetail(cart.getId())).thenReturn(Mono.just(orderDetail));

        // Act and Assert
        StepVerifier
            .create(cartService.checkout(userId))
            .expectError(IllegalStateException.class)
            .verify();

        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(userId);
        verify(orderService, times(1)).getOrderDetail(cart.getId());
    }

    @Test
    void testCheckoutCart() {
        // Arrange
        long userId = 13L;
        OrderWithProducts orderWithProducts = generateRandomOrderWithProducts();
        BigDecimal totalPrice = calculateOrderPrice(orderWithProducts);
        OrderDetail orderDetail = getOrderDetail(orderWithProducts);
        Order cart = generateRandomActiveOrder();
        cart.setId(orderWithProducts.getId());

        when(orderRepository.findByUserIdAndIsActiveTrue(anyLong())).thenReturn(Mono.just(cart));
        when(orderService.getOrderDetail(cart.getId())).thenReturn(Mono.just(orderDetail));
        when(paymentService.makePayment(anyLong(), any(BigDecimal.class)))
            .thenReturn(Mono.just(new Balance().balance(BigDecimal.TWO)));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(new Order()));

        // Act
        StepVerifier
            .create(cartService.checkout(userId))
            .verifyComplete();

        // Assert
        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(userId);
        verify(orderService, times(1)).getOrderDetail(cart.getId());
        verify(paymentService, times(1)).makePayment(userId, totalPrice);
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        cart.setIsActive(false);
        assertEquals(cart, orderCaptor.getValue());
    }

    @Test
    void testCheckoutCartInsufficientFunds() {
        // Arrange
        long userId = 13L;
        OrderWithProducts orderWithProducts = generateRandomOrderWithProducts();
        BigDecimal totalPrice = calculateOrderPrice(orderWithProducts);
        BigDecimal availableBalance = totalPrice.subtract(BigDecimal.ONE);
        OrderDetail orderDetail = getOrderDetail(orderWithProducts);
        Order cart = generateRandomActiveOrder();
        cart.setId(orderWithProducts.getId());

        when(orderRepository.findByUserIdAndIsActiveTrue(anyLong())).thenReturn(Mono.just(cart));
        when(orderService.getOrderDetail(cart.getId())).thenReturn(Mono.just(orderDetail));
        when(paymentService.makePayment(anyLong(), any(BigDecimal.class))).thenReturn(Mono.error(new InsufficientFundsException(
                HttpStatus.BAD_REQUEST, "Not enough funds to complete transaction", totalPrice, availableBalance)));

        // Act
        StepVerifier
            .create(cartService.checkout(userId))
            .expectError(InsufficientFundsException.class)
            .verify();

        // Assert
        verify(orderRepository, times(1)).findByUserIdAndIsActiveTrue(userId);
        verify(orderService, times(1)).getOrderDetail(cart.getId());
        verify(paymentService, times(1)).makePayment(userId, totalPrice);
        verify(orderRepository, never()).save(any(Order.class));
    }
}
