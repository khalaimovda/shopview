package com.github.khalaimovda.shopview.paymentservice.mapper;

import com.github.khalaimovda.shopview.paymentservice.domain.InsufficientFundsError;
import com.github.khalaimovda.shopview.paymentservice.exception.InsufficientFundsException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ErrorMapper {

    @Mapping(source = "message", target = "error")
    InsufficientFundsError toInsufficientFundsError(InsufficientFundsException ex, String message);
}
