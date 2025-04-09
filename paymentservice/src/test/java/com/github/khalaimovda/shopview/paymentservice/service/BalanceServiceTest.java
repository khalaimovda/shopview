package com.github.khalaimovda.shopview.paymentservice.service;

import com.github.khalaimovda.shopview.paymentservice.domain.Balance;
import com.github.khalaimovda.shopview.paymentservice.exception.InsufficientFundsException;
import com.github.khalaimovda.shopview.paymentservice.repository.BalanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceTest {

    @Mock
    private BalanceRepository repository;

    @InjectMocks
    private BalanceServiceImpl service;

    @Test
    void testGetBalance() {
        Balance actualBalance = new Balance().balance(BigDecimal.TEN);
        when(repository.getBalance()).thenReturn(Mono.just(actualBalance));

        StepVerifier
            .create(service.getBalance())
            .assertNext(balance -> assertEquals(actualBalance, balance))
            .verifyComplete();

        verify(repository, times(1)).getBalance();
    }

    @Test
    void testDecreaseBalance() {
        Balance actualBalance = new Balance().balance(BigDecimal.TEN);
        when(repository.getBalance()).thenReturn(Mono.just(actualBalance));
        BigDecimal amount = BigDecimal.TWO;
        Balance newBalance = new Balance().balance(actualBalance.getBalance().subtract(amount));
        when(repository.updateBalance(any(BigDecimal.class))).thenReturn(Mono.just(newBalance));

        StepVerifier
            .create(service.decreaseBalance(amount))
            .assertNext(balance -> assertEquals(newBalance, balance))
            .verifyComplete();

        verify(repository, times(1)).getBalance();
        verify(repository, times(1)).updateBalance(newBalance.getBalance());
    }

    @Test
    void testDecreaseBalanceInsufficientFunds() {
        Balance actualBalance = new Balance().balance(BigDecimal.TWO);
        when(repository.getBalance()).thenReturn(Mono.just(actualBalance));
        BigDecimal amount = BigDecimal.TEN;

        StepVerifier
            .create(service.decreaseBalance(amount))
            .expectError(InsufficientFundsException.class)
            .verify();

        verify(repository, times(1)).getBalance();
    }
}
