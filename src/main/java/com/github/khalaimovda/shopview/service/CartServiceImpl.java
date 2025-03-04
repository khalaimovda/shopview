package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.Cart;
import com.github.khalaimovda.shopview.dto.CartProduct;
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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderProductService orderProductService;
    private final OrderProductRepository orderProductRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public Optional<Cart> getCart() {
        Optional<Order> optionalOrder = orderRepository.findByIsActiveTrue();
        return optionalOrder.map(order -> {
            Cart cart = new Cart();
            cart.setOderId(order.getId());
            BigDecimal totalPrice = BigDecimal.ZERO;
            for (OrderProduct orderProduct : order.getOrderProducts()) {
                Product product = orderProduct.getProduct();  // todo: Проверить количество запросов
                CartProduct cartProduct = productMapper.toCartProduct(product, orderProduct.getCount());
                cart.getProducts().add(cartProduct);
                totalPrice = totalPrice.add(cartProduct.getTotalPrice());
            }
            cart.getProducts().sort(Comparator.comparing(CartProduct::getName));
            cart.setTotalPrice(totalPrice);
            return cart;
        });
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
