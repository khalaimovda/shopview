package com.github.khalaimovda.shopview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDetail {

    @NotNull
    private Long oderId;

    @NotNull
    private List<ProductOfOrder> products = new ArrayList<>();

    @NotNull
    private BigDecimal totalPrice = BigDecimal.ZERO;
}
