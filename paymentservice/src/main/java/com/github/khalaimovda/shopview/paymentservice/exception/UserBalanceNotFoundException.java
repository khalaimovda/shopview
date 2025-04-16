package com.github.khalaimovda.shopview.paymentservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
public class UserBalanceNotFoundException extends IllegalArgumentException {
    private final long userId;
}
