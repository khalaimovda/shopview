package com.github.khalaimovda.shopview.paymentservice.repository;

import com.github.khalaimovda.shopview.paymentservice.domain.Balance;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;


public interface BalanceRepository {
    Mono<Balance> getBalance();
    Mono<Balance> updateBalance(BigDecimal amount);
}
