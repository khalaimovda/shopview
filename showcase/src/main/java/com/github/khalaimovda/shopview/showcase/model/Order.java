package com.github.khalaimovda.shopview.showcase.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    private Long id;

    @Column("is_active")
    private Boolean isActive = true;

    @Column("user_id")
    private Long userId;
}
