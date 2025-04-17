package com.github.khalaimovda.shopview.paymentservice.service;

import com.github.khalaimovda.shopview.paymentservice.exception.InsufficientFundsException;
import com.github.khalaimovda.shopview.paymentservice.exception.UserBalanceNotFoundException;
import com.github.khalaimovda.shopview.paymentservice.model.Balance;
import com.github.khalaimovda.shopview.paymentservice.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final BalanceRepository repository;

    @Override
    @PreAuthorize("isAuthenticated() and hasAuthority('read')")
    public Mono<Balance> getBalance(long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    @PreAuthorize("isAuthenticated() and hasAuthority('write')")
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

    @Override
    @PreAuthorize("isAuthenticated() and hasAuthority('write')")
    public Mono<Balance> addBalance(long userId, BigDecimal amount) {
        return repository
            .findByUserId(userId)
            .switchIfEmpty(Mono.fromSupplier(() -> {
                    var balance = new com.github.khalaimovda.shopview.paymentservice.model.Balance();
                    balance.setUserId(userId);
                    balance.setBalance(BigDecimal.ZERO);
                    return balance;
                })
            )
            .flatMap(balance -> {
                    balance.setBalance(balance.getBalance().add(amount));
                    return repository.save(balance);
                }
            );
    }
}
