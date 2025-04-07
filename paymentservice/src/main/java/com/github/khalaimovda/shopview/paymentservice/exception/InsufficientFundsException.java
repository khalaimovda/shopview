package com.github.khalaimovda.shopview.paymentservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
public class InsufficientFundsException extends IllegalStateException {
    private final BigDecimal requiredAmount;
    private final BigDecimal availableBalance;
}
