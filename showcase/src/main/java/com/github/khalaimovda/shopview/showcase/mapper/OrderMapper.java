package com.github.khalaimovda.shopview.showcase.mapper;

import com.github.khalaimovda.shopview.showcase.dto.OrderDetail;
import com.github.khalaimovda.shopview.showcase.dto.OrderListItem;
import com.github.khalaimovda.shopview.showcase.dto.OrderWithProducts;
import com.github.khalaimovda.shopview.showcase.dto.ProductOfOrder;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {

    @Mappings({
        @Mapping(source = "oderId", target = "id"),
        @Mapping(source = "totalPrice", target = "price")
    })
    OrderListItem toOrderListItem(OrderDetail orderDetail);

    @Mapping(source = "products", target = "price", qualifiedByName = "calculateTotalPriceForProductsOfOrder")
    OrderListItem toOrderListItem(OrderWithProducts orderWithProducts);

    @Mappings({
        @Mapping(source = "id", target = "oderId"),
        @Mapping(source = "products", target = "products"),
        @Mapping(source = "products", target = "totalPrice", qualifiedByName = "calculateTotalPriceForProductsOfOrder")
    })
    OrderDetail toOrderDetail(OrderWithProducts orderWithProducts);

    @Named("calculateTotalPriceForProductsOfOrder")
    static BigDecimal calculateTotalPriceForProductsOfOrder(List<ProductOfOrder> products) {
        return products.stream()
            .map(ProductOfOrder::getTotalPrice)
            .reduce(BigDecimal::add)
            .orElseGet(() -> BigDecimal.ZERO);
    }
}
