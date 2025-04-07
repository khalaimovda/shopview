package com.github.khalaimovda.shopview.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderListItem {
    @NotNull
    @Min(1L)
    private Long id;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
}
