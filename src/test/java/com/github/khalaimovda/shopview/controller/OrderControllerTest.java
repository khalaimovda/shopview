package com.github.khalaimovda.shopview.controller;


import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.OrderListItem;
import com.github.khalaimovda.shopview.mapper.OrderMapper;
import com.github.khalaimovda.shopview.service.OrderService;
import com.github.khalaimovda.shopview.utils.OrderUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.khalaimovda.shopview.utils.OrderUtils.*;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProducts;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void testGetAllOrders() throws Exception {
        List<OrderListItem> orders = generateRandomOrders(
            Stream.generate(() -> generateRandomProducts(5)).limit(5).toList()
        ).stream()
            .map(OrderUtils::getOrderDetail)
            .map(orderMapper::toOrderListItem)
            .toList();

        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("orders"));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void testGetOrderById() throws Exception {
        long orderId = 13L;
        OrderDetail orderDetail = getOrderDetail(generateRandomNotActiveOrder(generateRandomProducts(5)));
        when(orderService.getOrderDetail(anyLong())).thenReturn(Optional.of(orderDetail));

        mockMvc.perform(get("/orders/" + orderId))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("order"));

        verify(orderService, times(1)).getOrderDetail(orderId);
    }
}
