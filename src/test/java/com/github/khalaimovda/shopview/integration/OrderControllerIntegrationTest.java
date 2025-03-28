package com.github.khalaimovda.shopview.integration;


import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testGetAllOrders() throws Exception {

        Order order = orderRepository.findById(1L).get();
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrderIn(List.of(order));
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderProduct orderProduct : orderProducts) {
            BigDecimal productPrice = productRepository.findById(orderProduct.getProduct().getId()).get().getPrice();
            totalPrice.add(productPrice.multiply(new BigDecimal(orderProduct.getCount())));
        }

        mockMvc.perform(get("/orders"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("orders"))
            .andExpect(content().string(not(containsString("Нет оформленных заказов"))))
            .andExpect(content().string(containsString("Заказ #1")))
            .andExpect(content().string(containsString(totalPrice + " ₽")));
    }

    @Test
    void testGetOrderById() throws Exception {
        long orderId = 1L;
        Order order = orderRepository.findById(orderId).get();
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrderIn(List.of(order));
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderProduct orderProduct : orderProducts) {
            BigDecimal productPrice = productRepository.findById(orderProduct.getProduct().getId()).get().getPrice();
            totalPrice.add(productPrice.multiply(new BigDecimal(orderProduct.getCount())));
        }

        mockMvc.perform(get("/orders/" + orderId))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("order"))
            .andExpect(content().string(containsString("Цена за единицу:")))
            .andExpect(content().string(containsString("Количество:")))
            .andExpect(content().string(containsString("Итого:")))
            .andExpect(content().string(containsString("Общая сумма:")))
            .andExpect(content().string(containsString(totalPrice.toString())));
    }
}
