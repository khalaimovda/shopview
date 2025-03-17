package com.github.khalaimovda.shopview.mapper;

import com.github.khalaimovda.shopview.dto.OrderDetail;
import com.github.khalaimovda.shopview.dto.OrderListItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {

    @Mappings({
        @Mapping(source = "oderId", target = "id"),
        @Mapping(source = "totalPrice", target = "price")
    })
    OrderListItem toOrderListItem(OrderDetail orderDetail);
}
