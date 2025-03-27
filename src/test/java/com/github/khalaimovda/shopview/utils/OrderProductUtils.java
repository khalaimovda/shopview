package com.github.khalaimovda.shopview.utils;

import com.github.khalaimovda.shopview.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class OrderProductUtils {

    private static final Random random = new Random();

    public static OrderProduct generateRandomOrderProduct(Order order, Product product) {
        OrderProductId orderProductId = new OrderProductId();
        orderProductId.setOrderId(order.getId());
        orderProductId.setProductId(product.getId());

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setId(orderProductId);
        orderProduct.setProduct(product);
        orderProduct.setOrder(order);
        orderProduct.setCount(random.nextInt(1, 10));

        return orderProduct;
    }

    public static List<OrderProduct> generateRandomOrderProducts(Order order, List<Product> products) {
        return products.stream().map(product -> generateRandomOrderProduct(order, product)).toList();
    }

    /**
     * Suppose that all order_product items belong to one order
     */
    public static BigDecimal calculateTotalPrice(List<OrderProduct> orderProducts) {
        return orderProducts.stream().map(
            orderProduct -> {
                BigDecimal price = orderProduct.getProduct().getPrice();
                Integer count = orderProduct.getCount();
                return price.multiply(new BigDecimal(count));
            }
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
