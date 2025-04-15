package com.github.khalaimovda.shopview.showcase.dto;


import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductListItem {
    @NotNull
    @Min(1L)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String imagePath;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @Nullable
    @Min(0)
    private Integer count;
}
