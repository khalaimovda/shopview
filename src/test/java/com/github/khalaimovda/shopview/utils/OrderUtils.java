package com.github.khalaimovda.shopview.utils;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.ProductOfOrder;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.OrderProductId;
import com.github.khalaimovda.shopview.model.Product;

import java.math.BigDecimal;
import java.util.Comparator;
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

    public static List<Order> generateRandomOrders(List<List<Product>> listOfProducts) {
        return listOfProducts.stream().map(OrderUtils::generateRandomNotActiveOrder).toList();
    }

    public static BigDecimal calculateOrderPrice(Order order) {
        return order.getOrderProducts().stream().map(
            orderProduct -> {
                BigDecimal price = orderProduct.getProduct().getPrice();
                Integer count = orderProduct.getCount();
                return price.multiply(new BigDecimal(count));
            }
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static List<ProductOfOrder> getProductOfOrderList(Order order) {
        return order.getOrderProducts().stream().map(
            orderProduct -> {
                Product product = orderProduct.getProduct();

                ProductOfOrder productOfOrder = new ProductOfOrder();
                productOfOrder.setId(product.getId());
                productOfOrder.setName(product.getName());
                productOfOrder.setPrice(product.getPrice());
                productOfOrder.setCount(orderProduct.getCount());
                productOfOrder.setTotalPrice(product.getPrice().multiply(new BigDecimal(orderProduct.getCount())));

                return productOfOrder;
            }
        ).toList();
    }

    public static OrderDetail getOrderDetail(Order order) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOderId(order.getId());
        orderDetail.setProducts(
            getProductOfOrderList(order).stream()
                .sorted(Comparator.comparing(ProductOfOrder::getName)).toList() // Sort products by name
        );
        orderDetail.setTotalPrice(calculateOrderPrice(order));
        return orderDetail;
    }
}
