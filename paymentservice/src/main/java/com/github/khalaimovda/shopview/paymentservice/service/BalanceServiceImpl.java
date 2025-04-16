package com.github.khalaimovda.shopview.paymentservice.service;

import com.github.khalaimovda.shopview.paymentservice.exception.InsufficientFundsException;
import com.github.khalaimovda.shopview.paymentservice.exception.UserBalanceNotFoundException;
import com.github.khalaimovda.shopview.paymentservice.model.Balance;
import com.github.khalaimovda.shopview.paymentservice.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final BalanceRepository repository;

    @Override
    public Mono<Balance> getBalance(long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Mono<Balance> decreaseBalance(long userId, BigDecimal amount) {
        return repository
            .findByUserId(userId)
            .switchIfEmpty(Mono.error(new UserBalanceNotFoundException(userId)))
            .flatMap(balance -> {
                if (amount.compareTo(balance.getBalance()) > 0) {
                    return Mono.error(new InsufficientFundsException(amount, balance.getBalance()));
                }
                balance.setBalance(balance.getBalance().subtract(amount));
                return repository.save(balance);
                }
            );
    }
}
