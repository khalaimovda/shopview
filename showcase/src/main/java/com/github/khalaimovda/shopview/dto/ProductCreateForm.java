package com.github.khalaimovda.shopview.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateForm {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private FilePart image;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
}
