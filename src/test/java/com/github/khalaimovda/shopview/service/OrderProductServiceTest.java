package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.khalaimovda.shopview.utils.OrderProductUtils.generateRandomOrderProduct;
import static com.github.khalaimovda.shopview.utils.OrderProductUtils.generateRandomOrderProducts;
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
    private ArgumentCaptor<List<Long>> productIdsCaptor;

    @Captor
    private ArgumentCaptor<OrderProduct> orderProductCaptor;

    @Test
    void testGetProductIdCountMap() {
        // Arrange
        List<Long> productIds = generateRandomProducts(5).stream().map(Product::getId).toList();
        Long orderId = generateRandomActiveOrder().getId();
        List<OrderProduct> orderProducts = generateRandomOrderProducts(orderId, productIds);
        when(orderProductRepository.findAllByOrderIdAndProductIdIn(anyLong(), anyList()))
            .thenReturn(Flux.just(orderProducts.toArray(new OrderProduct[0])));

        Map<Long, Integer> expectedProductIdCountMap = new HashMap<>();
        for (OrderProduct orderProduct : orderProducts) {
            long productId = orderProduct.getProductId();
            int count = orderProduct.getCount();
            expectedProductIdCountMap.putIfAbsent(productId, 0);
            expectedProductIdCountMap.put(productId, expectedProductIdCountMap.get(productId) + count);
        }

        // Act
        Mono<Map<Long, Integer>> monoProductIdCountMap = orderProductService.getProductIdCountMap(orderId, productIds);

        // Assert
        StepVerifier
            .create(monoProductIdCountMap)
            .assertNext(productIdCountMap -> assertEquals(expectedProductIdCountMap, productIdCountMap))
            .verifyComplete();

        verify(orderProductRepository, times(1))
            .findAllByOrderIdAndProductIdIn(eq(orderId), productIdsCaptor.capture());
        assertEquals(productIds, productIdsCaptor.getValue());
    }

    @Test
    void testAddProductToOrderOrderProductExists() {
        // Arrange
        Long productId = generateRandomProduct().getId();
        Long orderId = generateRandomNotActiveOrder().getId();
        OrderProduct orderProduct = generateRandomOrderProduct(orderId, productId);
        int count = orderProduct.getCount();
        when(orderProductRepository.findByOrderIdAndProductId(anyLong(), anyLong()))
            .thenReturn(Mono.just(orderProduct));
        when(orderProductRepository.save(any(OrderProduct.class))).thenReturn(Mono.just(orderProduct));

        // Act
        Mono<Void> monoResult = orderProductService.addProductToOrder(orderId, productId);

        // Assert
        StepVerifier.create(monoResult).verifyComplete();

        verify(orderProductRepository, times(1))
            .findByOrderIdAndProductId(orderId, productId);

        verify(orderProductRepository, times(1))
            .save(orderProductCaptor.capture());
        assertAll(
            () -> assertEquals(orderId, orderProductCaptor.getValue().getOrderId()),
            () -> assertEquals(productId, orderProductCaptor.getValue().getProductId()),
            () -> assertEquals(count + 1, orderProductCaptor.getValue().getCount())
        );
    }

    @Test
    void testAddProductToOrderOrderProductNotExist() {
        // Arrange
        Long productId = generateRandomProduct().getId();
        Long orderId = generateRandomNotActiveOrder().getId();
        when(orderProductRepository.findByOrderIdAndProductId(anyLong(), anyLong()))
            .thenReturn(Mono.empty());
        when(orderProductRepository.save(any(OrderProduct.class))).thenReturn(Mono.just(new OrderProduct()));

        // Act
        Mono<Void> monoResult = orderProductService.addProductToOrder(orderId, productId);

        // Assert
        StepVerifier.create(monoResult).verifyComplete();

        verify(orderProductRepository, times(1))
            .findByOrderIdAndProductId(orderId, productId);

        verify(orderProductRepository, times(1))
            .save(orderProductCaptor.capture());
        assertAll(
            () -> assertEquals(orderId, orderProductCaptor.getValue().getOrderId()),
            () -> assertEquals(productId, orderProductCaptor.getValue().getProductId()),
            () -> assertEquals(1, orderProductCaptor.getValue().getCount())
        );
    }

    @Test
    void testDecreaseProductInOrderOrderProductNotExist() {
        // Arrange
        Long productId = generateRandomProduct().getId();
        Long orderId = generateRandomNotActiveOrder().getId();
        when(orderProductRepository.findByOrderIdAndProductId(anyLong(), anyLong()))
            .thenReturn(Mono.empty());

        // Act
        Mono<Void> monoResult = orderProductService.decreaseProductInOrder(orderId, productId);

        // Assert
        StepVerifier.create(monoResult).verifyComplete();

        verify(orderProductRepository, times(1))
            .findByOrderIdAndProductId(orderId, productId);

        // Both operations save() and delete() must not be called
        verify(orderProductRepository, never()).save(any());
        verify(orderProductRepository, never()).delete(any());
    }

    @Test
    void testDecreaseProductInOrderOrderProductHasCountOne() {
        // Arrange
        Long productId = generateRandomProduct().getId();
        Long orderId = generateRandomNotActiveOrder().getId();
        OrderProduct orderProduct = generateRandomOrderProduct(orderId, productId);
        orderProduct.setCount(1);

        when(orderProductRepository.findByOrderIdAndProductId(anyLong(), anyLong()))
            .thenReturn(Mono.just(orderProduct));
        when(orderProductRepository.delete(any(OrderProduct.class))).thenReturn(Mono.empty());

        // Act
        Mono<Void> monoResult = orderProductService.decreaseProductInOrder(orderId, productId);

        // Assert
        StepVerifier.create(monoResult).verifyComplete();

        verify(orderProductRepository, times(1))
            .findByOrderIdAndProductId(orderId, productId);

        // Count == 1; decrease() => delete()
        verify(orderProductRepository, times(1)).delete(orderProductCaptor.capture());
        assertEquals(orderProduct, orderProductCaptor.getValue());

        // Operations save() must not be called
        verify(orderProductRepository, never()).save(any());
    }

    @Test
    void testDecreaseProductInOrderOrderProductHasCountMoreThanOne() {
        // Arrange
        Long productId = generateRandomProduct().getId();
        Long orderId = generateRandomNotActiveOrder().getId();
        OrderProduct orderProduct = generateRandomOrderProduct(orderId, productId);
        int count = 13;
        orderProduct.setCount(count); // Set count = 13 > 1
        when(orderProductRepository.findByOrderIdAndProductId(anyLong(), anyLong()))
            .thenReturn(Mono.just(orderProduct));
        when(orderProductRepository.save(any(OrderProduct.class))).thenReturn(Mono.just(new OrderProduct()));

        // Act
        Mono<Void> monoResult = orderProductService.decreaseProductInOrder(orderId, productId);

        // Assert
        StepVerifier.create(monoResult).verifyComplete();

        verify(orderProductRepository, times(1))
            .findByOrderIdAndProductId(orderId, productId);

        verify(orderProductRepository, times(1))
            .save(orderProductCaptor.capture());
        assertAll(
            () -> assertEquals(orderId, orderProductCaptor.getValue().getOrderId()),
            () -> assertEquals(productId, orderProductCaptor.getValue().getProductId()),
            () -> assertEquals(count - 1, orderProductCaptor.getValue().getCount())
        );

        // Operations delete() must not be called
        verify(orderProductRepository, never()).delete(any());
    }

    @Test
    void testRemoveProductInOrderOrderProductNotExist() {
        // Arrange
        Long productId = generateRandomProduct().getId();
        Long orderId = generateRandomNotActiveOrder().getId();
        OrderProduct orderProduct = generateRandomOrderProduct(orderId, productId);

        when(orderProductRepository.findByOrderIdAndProductId(anyLong(), anyLong()))
            .thenReturn(Mono.empty());

        // Act
        Mono<Void> monoResult = orderProductService.removeProductFromOrder(orderId, productId);

        // Assert
        StepVerifier.create(monoResult).verifyComplete();

        verify(orderProductRepository, times(1))
            .findByOrderIdAndProductId(orderId, productId);

        // Operation delete() must not be called
        verify(orderProductRepository, never()).delete(any());
    }

    @Test
    void testRemoveProductInOrder() {
        // Arrange
        Long productId = generateRandomProduct().getId();
        Long orderId = generateRandomNotActiveOrder().getId();
        OrderProduct orderProduct = generateRandomOrderProduct(orderId, productId);
        int count = orderProduct.getCount();
        when(orderProductRepository.findByOrderIdAndProductId(anyLong(), anyLong()))
            .thenReturn(Mono.just(orderProduct));
        when(orderProductRepository.delete(any(OrderProduct.class))).thenReturn(Mono.empty());

        // Act
        Mono<Void> monoResult = orderProductService.removeProductFromOrder(orderId, productId);

        // Assert
        StepVerifier.create(monoResult).verifyComplete();

        verify(orderProductRepository, times(1))
            .findByOrderIdAndProductId(orderId, productId);

        verify(orderProductRepository, times(1)).delete(orderProductCaptor.capture());
        assertEquals(orderProduct, orderProductCaptor.getValue());
    }
}
