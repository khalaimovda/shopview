package com.github.khalaimovda.shopview.utils;

import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.OrderProductId;
import com.github.khalaimovda.shopview.model.Product;

import java.util.List;
import java.util.Random;

public class OrderUtils {

    private static final Random random = new Random();

    public static Order generateRandomNotActiveOrder(List<Product> products) {
        Order order = new Order();
        order.setId(random.nextLong(1, Long.MAX_VALUE));
        order.setIsActive(false);

        List<OrderProduct> orderProducts = products.stream()
            .map(product -> {
                OrderProductId orderProductId = new OrderProductId();
                orderProductId.setOrderId(order.getId());
                orderProductId.setProductId(product.getId());

                OrderProduct orderProduct = new OrderProduct();
                orderProduct.setId(orderProductId);
                orderProduct.setProduct(product);
                orderProduct.setOrder(order);
                orderProduct.setCount(random.nextInt(1, 10));

                return orderProduct;
            }).toList();
        order.setOrderProducts(orderProducts);

        return order;
    }

    public static Order generateRandomActiveOrder(List<Product> products) {
        Order order = generateRandomNotActiveOrder(products);
        order.setIsActive(true);
        return order;
    }
}
