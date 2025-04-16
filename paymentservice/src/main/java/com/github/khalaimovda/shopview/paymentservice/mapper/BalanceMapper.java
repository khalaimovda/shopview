package com.github.khalaimovda.shopview.paymentservice.mapper;

import com.github.khalaimovda.shopview.paymentservice.model.Balance;
import com.github.khalaimovda.shopview.paymentservice.domain.PaymentSuccessResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BalanceMapper {
    PaymentSuccessResponse toPaymentSuccessResponse(Balance balance);
}
