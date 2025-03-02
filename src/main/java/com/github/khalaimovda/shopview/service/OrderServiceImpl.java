package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderProductService orderProductService;

    @Override
    @Transactional
    public void addProductToCart(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            throw new NoSuchElementException(String.format("Product with id %s is not found", productId));
        }
        Product product = optionalProduct.get();
        Order activeOrder = getOrCreateActiveOrder();
        orderProductService.addProductToOrder(activeOrder, product);
    }

    @Transactional
    private Order getOrCreateActiveOrder() {
        Optional<Order> activeOrder = orderRepository.findByIsActiveTrue();
        return activeOrder.orElseGet(() -> orderRepository.save(new Order()));
    }
}
