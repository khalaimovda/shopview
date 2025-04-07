package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.showcase.domain.Balance;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PaymentService {
    Mono<Balance> getBalance();
    Mono<Balance> makePayment(BigDecimal amount);
}
