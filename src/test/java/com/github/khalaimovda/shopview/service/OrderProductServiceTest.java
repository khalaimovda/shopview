package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.OrderProductId;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.khalaimovda.shopview.utils.OrderUtils.generateRandomActiveOrder;
import static com.github.khalaimovda.shopview.utils.OrderUtils.generateRandomNotActiveOrder;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProduct;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderProductServiceTest {

    @Mock
    private OrderProductRepository orderProductRepository;

    @InjectMocks
    private OrderProductServiceImpl orderProductService;

    @Captor
    private ArgumentCaptor<OrderProductId> orderProductIdCaptor;

    @Captor
    private ArgumentCaptor<OrderProduct> orderProductCaptor;

    @Test
    void testGetProductIdCountMap() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomActiveOrder(products);
        List<OrderProduct> orderProducts = order.getOrderProducts();
        when(orderProductRepository.findAllByOrderAndProductIn(order, products)).thenReturn(orderProducts);

        Map<Long, Integer> expectedProductIdCountMap = new HashMap<>();
        for (OrderProduct orderProduct : orderProducts) {
            long productId = orderProduct.getProduct().getId();
            int count = orderProduct.getCount();
            expectedProductIdCountMap.putIfAbsent(productId, 0);
            expectedProductIdCountMap.put(productId, expectedProductIdCountMap.get(productId) + count);
        }

        // Act
        Map<Long, Integer> productIdCountMap = orderProductService.getProductIdCountMap(order, products);

        // Assert
        assertEquals(expectedProductIdCountMap, productIdCountMap);
    }

    @Test
    void testAddProductToOrderOrderProductExists() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder(products);
        Product product = products.getFirst();
        OrderProduct orderProduct = order.getOrderProducts().getFirst();
        int count = orderProduct.getCount();
        when(orderProductRepository.findById(any())).thenReturn(Optional.of(orderProduct));

        // Act
        orderProductService.addProductToOrder(order, product);

        // Assert
        verify(orderProductRepository, times(1)).findById(orderProductIdCaptor.capture());
        assertAll(
            () -> assertEquals(order.getId(), orderProductIdCaptor.getValue().getOrderId()),
            () -> assertEquals(product.getId(), orderProductIdCaptor.getValue().getProductId())
        );

        verify(orderProductRepository, times(1)).save(orderProductCaptor.capture());
        assertAll(
            () -> assertEquals(order, orderProductCaptor.getValue().getOrder()),
            () -> assertEquals(product, orderProductCaptor.getValue().getProduct()),
            () -> assertEquals(order.getId(), orderProductCaptor.getValue().getId().getOrderId()),
            () -> assertEquals(product.getId(), orderProductCaptor.getValue().getId().getProductId()),
            () -> assertEquals(count + 1, orderProductCaptor.getValue().getCount())
        );
    }

    @Test
    void testAddProductToOrderOrderProductNotExist() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder(products);
        Product product = generateRandomProduct();
        when(orderProductRepository.findById(any())).thenReturn(Optional.empty());

        // Act
        orderProductService.addProductToOrder(order, product);

        // Assert
        verify(orderProductRepository, times(1)).findById(orderProductIdCaptor.capture());
        assertAll(
            () -> assertEquals(order.getId(), orderProductIdCaptor.getValue().getOrderId()),
            () -> assertEquals(product.getId(), orderProductIdCaptor.getValue().getProductId())
        );

        verify(orderProductRepository, times(1)).save(orderProductCaptor.capture());
        assertAll(
            () -> assertEquals(order, orderProductCaptor.getValue().getOrder()),
            () -> assertEquals(product, orderProductCaptor.getValue().getProduct()),
            () -> assertEquals(order.getId(), orderProductCaptor.getValue().getId().getOrderId()),
            () -> assertEquals(product.getId(), orderProductCaptor.getValue().getId().getProductId()),
            () -> assertEquals(1, orderProductCaptor.getValue().getCount())
        );
    }

    @Test
    void testDecreaseProductInOrderOrderProductNotExist() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder(products);
        Product product = generateRandomProduct();
        when(orderProductRepository.findById(any())).thenReturn(Optional.empty());

        // Act
        orderProductService.decreaseProductInOrder(order, product);

        // Assert
        verify(orderProductRepository, times(1)).findById(orderProductIdCaptor.capture());
        assertAll(
            () -> assertEquals(order.getId(), orderProductIdCaptor.getValue().getOrderId()),
            () -> assertEquals(product.getId(), orderProductIdCaptor.getValue().getProductId())
        );

        // Both operations save() and delete() must not be called
        verify(orderProductRepository, never()).save(any());
        verify(orderProductRepository, never()).delete(any());
    }

    @Test
    void testDecreaseProductInOrderOrderProductHasCountOne() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder(products);
        Product product = products.getFirst();
        OrderProduct orderProduct = order.getOrderProducts().getFirst();
        orderProduct.setCount(1); // Set count = 1
        when(orderProductRepository.findById(any())).thenReturn(Optional.of(orderProduct));

        // Act
        orderProductService.decreaseProductInOrder(order, product);

        // Assert
        verify(orderProductRepository, times(1)).findById(orderProductIdCaptor.capture());
        assertAll(
            () -> assertEquals(order.getId(), orderProductIdCaptor.getValue().getOrderId()),
            () -> assertEquals(product.getId(), orderProductIdCaptor.getValue().getProductId())
        );

        // Count == 1; decrease() => delete()
        verify(orderProductRepository, times(1)).delete(orderProductCaptor.capture());
        assertEquals(orderProduct, orderProductCaptor.getValue());

        // Operations save() must not be called
        verify(orderProductRepository, never()).save(any());
    }

    @Test
    void testDecreaseProductInOrderOrderProductHasCountMoreThanOne() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder(products);
        Product product = products.getFirst();
        OrderProduct orderProduct = order.getOrderProducts().getFirst();
        int count = 13;
        orderProduct.setCount(count); // Set count = 13 > 1
        when(orderProductRepository.findById(any())).thenReturn(Optional.of(orderProduct));

        // Act
        orderProductService.decreaseProductInOrder(order, product);

        // Assert
        verify(orderProductRepository, times(1)).findById(orderProductIdCaptor.capture());
        assertAll(
            () -> assertEquals(order.getId(), orderProductIdCaptor.getValue().getOrderId()),
            () -> assertEquals(product.getId(), orderProductIdCaptor.getValue().getProductId())
        );

        // Count == 1; decrease() => save(count - 1)
        verify(orderProductRepository, times(1)).save(orderProductCaptor.capture());
        assertAll(
            () -> assertEquals(order, orderProductCaptor.getValue().getOrder()),
            () -> assertEquals(product, orderProductCaptor.getValue().getProduct()),
            () -> assertEquals(order.getId(), orderProductCaptor.getValue().getId().getOrderId()),
            () -> assertEquals(product.getId(), orderProductCaptor.getValue().getId().getProductId()),
            () -> assertEquals(count - 1, orderProductCaptor.getValue().getCount())
        );

        // Operations delete() must not be called
        verify(orderProductRepository, never()).delete(any());
    }

    @Test
    void testRemoveProductInOrderOrderProductNotExist() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder(products);
        Product product = generateRandomProduct();
        when(orderProductRepository.findById(any())).thenReturn(Optional.empty());

        // Act
        orderProductService.removeProductFromOrder(order, product);

        // Assert
        verify(orderProductRepository, times(1)).findById(orderProductIdCaptor.capture());
        assertAll(
            () -> assertEquals(order.getId(), orderProductIdCaptor.getValue().getOrderId()),
            () -> assertEquals(product.getId(), orderProductIdCaptor.getValue().getProductId())
        );

        // Operation delete() must not be called
        verify(orderProductRepository, never()).delete(any());
    }

    @Test
    void testRemoveProductInOrder() {
        // Arrange
        List<Product> products = generateRandomProducts(5);
        Order order = generateRandomNotActiveOrder(products);
        Product product = products.getFirst();
        OrderProduct orderProduct = order.getOrderProducts().getFirst();
        when(orderProductRepository.findById(any())).thenReturn(Optional.of(orderProduct));

        // Act
        orderProductService.removeProductFromOrder(order, product);

        // Assert
        verify(orderProductRepository, times(1)).findById(orderProductIdCaptor.capture());
        assertAll(
            () -> assertEquals(order.getId(), orderProductIdCaptor.getValue().getOrderId()),
            () -> assertEquals(product.getId(), orderProductIdCaptor.getValue().getProductId())
        );

        verify(orderProductRepository, times(1)).delete(orderProductCaptor.capture());
        assertEquals(orderProduct, orderProductCaptor.getValue());
    }
}
