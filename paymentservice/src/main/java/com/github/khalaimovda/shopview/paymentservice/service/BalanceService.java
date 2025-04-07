package com.github.khalaimovda.shopview.paymentservice.service;

import com.github.khalaimovda.shopview.paymentservice.domain.Balance;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface BalanceService {

    Mono<Balance> getBalance();
    Mono<Balance> decreaseBalance(BigDecimal amount);
}
