package com.github.khalaimovda.shopview.showcase.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

import java.math.BigDecimal;


@Getter
public class InsufficientFundsException extends PaymentServiceException {
    private final BigDecimal requiredAmount;
    private final BigDecimal availableBalance;

    public InsufficientFundsException(HttpStatusCode status, String reason, BigDecimal requiredAmount, BigDecimal availableBalance) {
        super(status, reason);
        this.requiredAmount = requiredAmount;
        this.availableBalance = availableBalance;
    }
}
