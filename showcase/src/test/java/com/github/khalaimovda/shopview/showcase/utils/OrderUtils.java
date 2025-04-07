package com.github.khalaimovda.shopview.showcase.utils;

import com.github.khalaimovda.shopview.showcase.dto.OrderDetail;
import com.github.khalaimovda.shopview.showcase.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.showcase.dto.ProductOfOrder;
import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static com.github.khalaimovda.shopview.showcase.utils.OrderProductUtils.calculateTotalPrice;
import static com.github.khalaimovda.shopview.showcase.utils.OrderProductUtils.generateRandomOrderProduct;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProducts;

public class OrderUtils {

    private static final Random random = new Random();

    public static Order generateRandomNotActiveOrder() {
        Order order = new Order();
        order.setId(random.nextLong(1, Long.MAX_VALUE));
        order.setIsActive(false);
        return order;
    }

    public static Order generateRandomActiveOrder() {
        Order order = new Order();
        order.setId(random.nextLong(1, Long.MAX_VALUE));
        order.setIsActive(true);
        return order;
    }

    public static List<Order> generateRandomOrders(int count) {
        return Stream.generate(OrderUtils::generateRandomNotActiveOrder).limit(count).toList();
    }

    public static OrderWithProducts generateRandomOrderWithProducts(long orderId, List<Product> products) {
        List<ProductOfOrder> productsOfOrder = new ArrayList<>();
        products.forEach(
            product -> {
                int count = generateRandomOrderProduct(orderId, product.getId()).getCount();
                ProductOfOrder productOfOrder = new ProductOfOrder();
                productOfOrder.setId(product.getId());
                productOfOrder.setName(product.getName());
                productOfOrder.setPrice(product.getPrice());
                productOfOrder.setCount(count);
                productOfOrder.setTotalPrice(product.getPrice().multiply(new BigDecimal(count)));
                productsOfOrder.add(productOfOrder);
            }
        );

        OrderWithProducts orderWithProducts = new OrderWithProducts();
        orderWithProducts.setId(orderId);
        orderWithProducts.setProducts(productsOfOrder);

        return orderWithProducts;
    }


    public static OrderWithProducts generateRandomOrderWithProducts() {
        Order order = generateRandomNotActiveOrder();
        List<Product> products = generateRandomProducts(4);
        return generateRandomOrderWithProducts(order.getId(), products);
    }

    public static List<OrderWithProducts> generateRandomOrdersWithProducts(int count) {
        return Stream.generate(OrderUtils::generateRandomOrderWithProducts).limit(count).toList();
    }


    public static BigDecimal calculateOrderPrice(OrderWithProducts order) {
        return calculateTotalPrice(order.getProducts());
    }

    public static OrderDetail getOrderDetail(OrderWithProducts order) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOderId(order.getId());
        orderDetail.setProducts(order.getProducts());
        orderDetail.setTotalPrice(calculateOrderPrice(order));
        return orderDetail;
    }
}
