package com.github.khalaimovda.shopview.utils;

import com.github.khalaimovda.shopview.dto.ProductOfOrder;
import com.github.khalaimovda.shopview.model.OrderProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class OrderProductUtils {

    private static final Random random = new Random();

    public static OrderProduct generateRandomOrderProduct(Long orderId, Long productId) {
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setId(random.nextLong(1, Long.MAX_VALUE));
        orderProduct.setProductId(productId);
        orderProduct.setOrderId(orderId);
        orderProduct.setCount(random.nextInt(1, 10));
        return orderProduct;
    }

    public static List<OrderProduct> generateRandomOrderProducts(Long orderId, List<Long> productIds) {
        return productIds.stream().map(productId -> generateRandomOrderProduct(orderId, productId)).toList();
    }

    /**
     * Suppose that all order_product items belong to one order
     */
    public static BigDecimal calculateTotalPrice(List<ProductOfOrder> products) {
        return products.stream().map(ProductOfOrder::getTotalPrice).reduce(BigDecimal::add).get();
    }
}
