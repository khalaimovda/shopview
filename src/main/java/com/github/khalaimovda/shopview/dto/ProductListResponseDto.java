package com.github.khalaimovda.shopview.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductListResponseDto {
    @NotNull
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private String imagePath;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @Min(0)
    private Integer count;
}
