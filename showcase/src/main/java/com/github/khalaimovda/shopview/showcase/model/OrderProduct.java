package com.github.khalaimovda.shopview.showcase.model;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table(name = "order_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct {

    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("product_id")
    private Long productId;

    @Column
    @Min(1)
    private Integer count;

    public OrderProduct(Long orderId, Long productId, Integer count) {
        this.orderId = orderId;
        this.productId = productId;
        this.count = count;
    }

    public void incrementCount() {
        count++;
    }

    public void decrementCount() {
        if (count == 1) {
            throw new IllegalStateException("Count can not me less than 1");
        }
        count--;
    }
}
