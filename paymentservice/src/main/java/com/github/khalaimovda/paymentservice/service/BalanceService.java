package com.github.khalaimovda.paymentservice.service;

import com.github.khalaimovda.paymentservice.domain.Balance;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface BalanceService {

    Mono<Balance> getBalance();
    Mono<Balance> decreaseBalance(BigDecimal amount);
}
