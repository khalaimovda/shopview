package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderProductService orderProductService;
    private final OrderService orderService;

    @Override
    @Transactional
    @Cacheable(value = "orders", key = "'cart'")
    public Optional<OrderDetail> getCart() {
        Optional<Order> optionalOrder = orderRepository.findByIsActiveTrue().blockOptional();
        return optionalOrder.map(orderService::getOrderDetail);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    public void addProductToCart(Long productId) {
        Product product = getProductByIdOrNoSuchElementException(productId);
        Order activeOrder = getOrCreateActiveOrder();
        orderProductService.addProductToOrder(activeOrder, product);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    public void decreaseProductInCart(Long productId) {
        Product product = getProductByIdOrNoSuchElementException(productId);
        Order activeOrder = getActiveOrderOrNoSuchElementException();
        orderProductService.decreaseProductInOrder(activeOrder, product);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    public void removeProductFromCart(Long productId) {
        Product product = getProductByIdOrNoSuchElementException(productId);
        Order activeOrder = getActiveOrderOrNoSuchElementException();
        orderProductService.removeProductFromOrder(activeOrder, product);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "orders", allEntries = true)
    })
    public void checkout() {
        Optional<Order> optionalOrder = orderRepository.findByIsActiveTrue().blockOptional();
        Order order = optionalOrder.orElseThrow(() -> new NoSuchElementException("Cart does not exist"));
        validateCartIsNotEmpty(order);
        order.setIsActive(false);
        orderRepository.save(order);
    }

    @Transactional
    private Order getOrCreateActiveOrder() {
        Optional<Order> activeOrder = orderRepository.findByIsActiveTrue().blockOptional();
        return activeOrder.orElseGet(() -> orderRepository.save(new Order()).block());
    }

    private Product getProductByIdOrNoSuchElementException(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId).blockOptional();
        return optionalProduct.orElseThrow(
            () -> new NoSuchElementException(String.format("Product with id %s is not found", productId))
        );
    }

    private Order getActiveOrderOrNoSuchElementException() {
        Optional<Order> optionalActiveOrder = orderRepository.findByIsActiveTrue().blockOptional();
        return optionalActiveOrder.orElseThrow(
            () -> new NoSuchElementException("Active order is not found")
        );
    }

    private void validateCartIsNotEmpty(Order order) {
        OrderDetail orderDetail = orderService.getOrderDetail(order);
        if (orderDetail.getTotalPrice().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Cart is empty");
        }
    }
}
