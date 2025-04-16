package com.github.khalaimovda.shopview.paymentservice.model;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "balance")
@Data
public class Balance {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column
    @DecimalMin(value = "0.0")
    private BigDecimal balance;
}
