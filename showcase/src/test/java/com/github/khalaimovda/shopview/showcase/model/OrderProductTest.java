package com.github.khalaimovda.shopview.showcase.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.khalaimovda.shopview.showcase.utils.OrderProductUtils.generateRandomOrderProduct;
import static com.github.khalaimovda.shopview.showcase.utils.OrderUtils.generateRandomNotActiveOrder;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderProductTest {

    private OrderProduct orderProduct;

    @BeforeEach
    void setUp() {
        List<Product> products = generateRandomProducts(5);
        Product product = products.getFirst();
        Order order = generateRandomNotActiveOrder();
        this.orderProduct = generateRandomOrderProduct(order.getId(), product.getId());
    }

    @Test
    void testIncrementCount() {
        orderProduct.setCount(0);

        orderProduct.incrementCount();
        assertEquals(1, orderProduct.getCount());

        orderProduct.incrementCount();
        assertEquals(2, orderProduct.getCount());

        for (int i = 0; i < 15; i++) {
            orderProduct.incrementCount();
        }
        assertEquals(17, orderProduct.getCount());
    }

    @Test
    void testDecrementCountFromOne() {
        orderProduct.setCount(1);
        assertThrows(IllegalStateException.class, orderProduct::decrementCount);
    }

    @Test
    void testDecrementCount() {
        orderProduct.setCount(15);

        orderProduct.decrementCount();
        assertEquals(14, orderProduct.getCount());

        orderProduct.decrementCount();
        assertEquals(13, orderProduct.getCount());

        for (int i = 0; i < 10; i++) {
            orderProduct.decrementCount();
        }
        assertEquals(3, orderProduct.getCount());
    }
}
