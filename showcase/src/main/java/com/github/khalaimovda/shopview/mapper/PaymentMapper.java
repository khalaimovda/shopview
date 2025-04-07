package com.github.khalaimovda.shopview.mapper;

import com.github.khalaimovda.shopview.exception.InsufficientFundsException;
import com.github.khalaimovda.showcase.domain.Balance;
import com.github.khalaimovda.showcase.domain.InsufficientFundsError;
import com.github.khalaimovda.showcase.domain.PaymentSuccessResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.springframework.http.HttpStatusCode;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PaymentMapper {
    Balance toBalance(PaymentSuccessResponse successResponse);

    @Mapping(source = "reason", target = "error")
    InsufficientFundsError toInsufficientFundsError(InsufficientFundsException exception);

    @Mappings({
        @Mapping(source = "err.error", target = "reason"),
        @Mapping(source = "status", target = "status"),
        @Mapping(target = "stackTrace", ignore = true)
    })
    InsufficientFundsException toInsufficientFundsException(InsufficientFundsError err, HttpStatusCode status);
}
