package com.github.khalaimovda.shopview.showcase.mapper;

import com.github.khalaimovda.shopview.showcase.dto.ProductOfOrder;
import com.github.khalaimovda.shopview.showcase.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.showcase.dto.ProductDetail;
import com.github.khalaimovda.shopview.showcase.dto.ProductListItem;
import com.github.khalaimovda.shopview.showcase.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductMapper {

    @Mappings({
        @Mapping(source = "imagePath", target = "imagePath"),
        @Mapping(source = "count", target = "count")
    })
    ProductListItem toProductListItem(Product product, String imagePath, Integer count);

    @Mappings({
        @Mapping(source = "imagePath", target = "imagePath"),
        @Mapping(source = "count", target = "count")
    })
    ProductDetail toProductDetail(Product product, String imagePath, Integer count);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(source = "imagePath", target = "imagePath"),
    })
    Product toProduct(ProductCreateForm form, String imagePath);

    @Mappings({
        @Mapping(target = "totalPrice", expression = "java(product.getPrice().multiply(java.math.BigDecimal.valueOf(count)))"),
    })
    ProductOfOrder toProductOfOrder(Product product, Integer count);
}
