package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.mapper.ProductMapper;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderProductService orderProductService;
    private final OrderService orderService;
    private final OrderProductRepository orderProductRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public Optional<OrderDetail> getCart() {
        Optional<Order> optionalOrder = orderRepository.findByIsActiveTrue();
        return optionalOrder.map(orderService::getOrderDetail);
    }

    @Override
    @Transactional
    public void addProductToCart(Long productId) {
        Product product = getProductByIdOrNoSuchElementException(productId);
        Order activeOrder = getOrCreateActiveOrder();
        orderProductService.addProductToOrder(activeOrder, product);
    }

    @Override
    @Transactional
    public void decreaseProductInCart(Long productId) {
        Product product = getProductByIdOrNoSuchElementException(productId);
        Order activeOrder = getActiveOrderOrNoSuchElementException();
        orderProductService.decreaseProductInOrder(activeOrder, product);
    }

    @Override
    @Transactional
    public void removeProductFromCart(Long productId) {
        Product product = getProductByIdOrNoSuchElementException(productId);
        Order activeOrder = getActiveOrderOrNoSuchElementException();
        orderProductService.removeProductFromOrder(activeOrder, product);
    }

    @Override
    @Transactional
    public void checkout() {
        // todo: Проверка, что корзина не пустая
        Optional<Order> optionalOrder = orderRepository.findByIsActiveTrue();
        Order order = optionalOrder.orElseThrow(() -> new NoSuchElementException("Cart does not exist"));
        order.setIsActive(false);
        orderRepository.save(order);
    }

    @Transactional
    private Order getOrCreateActiveOrder() {
        Optional<Order> activeOrder = orderRepository.findByIsActiveTrue();
        return activeOrder.orElseGet(() -> orderRepository.save(new Order()));
    }

    private Product getProductByIdOrNoSuchElementException(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        return optionalProduct.orElseThrow(
            () -> new NoSuchElementException(String.format("Product with id %s is not found", productId))
        );
    }

    private Order getActiveOrderOrNoSuchElementException() {
        Optional<Order> optionalActiveOrder = orderRepository.findByIsActiveTrue();
        return optionalActiveOrder.orElseThrow(
            () -> new NoSuchElementException("Active order is not found")
        );
    }
}
