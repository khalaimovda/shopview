package com.github.khalaimovda.shopview.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_product")
@Getter
@Setter
public class OrderProduct {

    @EmbeddedId
    private OrderProductId id;

    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "count", nullable = false)
    @Min(1)
    private Integer count;

    public OrderProduct(Order order, Product product, Integer count) {
        this.order = order;
        this.product = product;
        this.count = count;
        this.id = new OrderProductId(order.getId(), product.getId());
    }
}
