package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.domain.Balance;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PaymentService {
    Mono<Balance> getBalance(long userId);
    Mono<Balance> addBalance(long userId, BigDecimal amount);
    Mono<Balance> makePayment(long userId, BigDecimal amount);
}
