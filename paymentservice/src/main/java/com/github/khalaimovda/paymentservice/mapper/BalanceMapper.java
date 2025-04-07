package com.github.khalaimovda.paymentservice.mapper;

import com.github.khalaimovda.paymentservice.domain.Balance;
import com.github.khalaimovda.paymentservice.domain.PaymentSuccessResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BalanceMapper {
    PaymentSuccessResponse toPaymentSuccessResponse(Balance balance);
}
