package com.github.khalaimovda.shopview.paymentservice.service;

import com.github.khalaimovda.shopview.paymentservice.domain.Balance;
import com.github.khalaimovda.shopview.paymentservice.exception.InsufficientFundsException;
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
    public Mono<Balance> getBalance() {
        return repository
            .getBalance();
    }

    @Override
    public Mono<Balance> decreaseBalance(BigDecimal amount) {
        return repository
            .getBalance()
            .flatMap(balance -> {
                if (amount.compareTo(balance.getBalance()) > 0) {
                    return Mono.error(new InsufficientFundsException(amount, balance.getBalance()));
                }
                return repository.updateBalance(balance.getBalance().subtract(amount));
            });
    }
}
