package com.github.khalaimovda.shopview.model;


import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;


@Table(name = "products")
@Getter
@Setter
public class Product {
    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @Column("image_path")
    private String imagePath;

    @Column
    @DecimalMin(value = "0.01")
    private BigDecimal price;
}
