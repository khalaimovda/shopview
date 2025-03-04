package com.github.khalaimovda.shopview.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartProduct {
    @NotNull
    @Min(1L)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @NotNull
    @Min(1)
    private Integer count;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal totalPrice;
}
