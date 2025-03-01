package com.github.khalaimovda.shopview.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class ProductCreateForm {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private MultipartFile image;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
}
