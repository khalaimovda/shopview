package com.github.khalaimovda.shopview.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "order_product")
@Getter
@Setter
@NoArgsConstructor
public class OrderProduct {

    @EmbeddedId
    private OrderProductId id = new OrderProductId();

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
