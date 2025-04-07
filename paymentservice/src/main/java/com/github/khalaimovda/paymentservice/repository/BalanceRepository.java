package com.github.khalaimovda.paymentservice.repository;

import com.github.khalaimovda.paymentservice.domain.Balance;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;


public interface BalanceRepository {
    Mono<Balance> getBalance();
    Mono<Balance> updateBalance(BigDecimal amount);
}
