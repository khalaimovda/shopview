package com.github.khalaimovda.paymentservice.repository;

import com.github.khalaimovda.paymentservice.domain.Balance;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;


@Repository
@Getter
public class ConfigBasedBalanceRepository implements BalanceRepository {

    private BigDecimal balance;

    public ConfigBasedBalanceRepository(@Value("${app.balance.amount}") BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public Mono<Balance> getBalance() {
        return Mono.just(createBalanceObj());
    }

    @Override
    public Mono<Balance> updateBalance(BigDecimal amount) {
        balance = amount;
        return Mono.just(createBalanceObj());
    }

    private Balance createBalanceObj() {
        Balance balance = new Balance();
        balance.setBalance(this.balance);
        return balance;
    }
}
