package com.github.khalaimovda.shopview.paymentservice.service;

import com.github.khalaimovda.shopview.paymentservice.model.Balance;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface BalanceService {
    Mono<Balance> getBalance(long userId);
    Mono<Balance> decreaseBalance(long userId, BigDecimal amount);
}
