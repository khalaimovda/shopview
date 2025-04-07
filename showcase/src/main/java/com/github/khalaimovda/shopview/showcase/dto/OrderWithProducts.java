package com.github.khalaimovda.shopview.showcase.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class OrderWithProducts {

    @NotNull
    private Long id;

    @NotNull
    private List<ProductOfOrder> products = new ArrayList<>();
}
