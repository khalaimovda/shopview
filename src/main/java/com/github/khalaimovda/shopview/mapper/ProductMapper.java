package com.github.khalaimovda.shopview.mapper;

import com.github.khalaimovda.shopview.dto.ProductListResponseDto;
import com.github.khalaimovda.shopview.model.Product;
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
    ProductListResponseDto toProductListResponseDto(Product product, String imagePath, Integer count);
}
