package com.github.khalaimovda.shopview.paymentservice.service;

import com.github.khalaimovda.shopview.paymentservice.exception.InsufficientFundsException;
import com.github.khalaimovda.shopview.paymentservice.model.Balance;
import com.github.khalaimovda.shopview.paymentservice.repository.BalanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceTest {

    @Mock
    private BalanceRepository repository;

    @InjectMocks
    private BalanceServiceImpl service;

    @Captor
    private ArgumentCaptor<Balance> balanceCaptor;

    @Test
    void testGetBalance() {
        long userId = 13L;
        Balance actualBalance = new Balance();
        actualBalance.setId(15L);
        actualBalance.setBalance(BigDecimal.TEN);
        actualBalance.setUserId(userId);
        when(repository.findByUserId(anyLong())).thenReturn(Mono.just(actualBalance));

        StepVerifier
            .create(service.getBalance(userId))
            .assertNext(balance -> assertEquals(actualBalance, balance))
            .verifyComplete();

        verify(repository, times(1)).findByUserId(userId);
    }

    @Test
    void testDecreaseBalance() {
        long userId = 13L;
        Balance actualBalance = new Balance();
        actualBalance.setId(15L);
        actualBalance.setBalance(BigDecimal.TEN);
        actualBalance.setUserId(userId);
        when(repository.findByUserId(anyLong())).thenReturn(Mono.just(actualBalance));
        BigDecimal amount = BigDecimal.TWO;

        Balance newBalance = new Balance();
        newBalance.setId(actualBalance.getId());
        newBalance.setUserId(actualBalance.getUserId());
        newBalance.setBalance(actualBalance.getBalance().subtract(amount));

        when(repository.save(any(Balance.class))).thenReturn(Mono.just(newBalance));

        StepVerifier
            .create(service.decreaseBalance(userId, amount))
            .assertNext(balance -> assertEquals(newBalance, balance))
            .verifyComplete();

        verify(repository, times(1)).findByUserId(userId);
        verify(repository, times(1)).save(balanceCaptor.capture());

        assertEquals(newBalance, balanceCaptor.getValue());
    }

    @Test
    void testDecreaseBalanceInsufficientFunds() {
        long userId = 13L;
        Balance actualBalance = new Balance();
        actualBalance.setId(15L);
        actualBalance.setBalance(BigDecimal.TWO);
        actualBalance.setUserId(userId);
        when(repository.findByUserId(anyLong())).thenReturn(Mono.just(actualBalance));
        BigDecimal amount = BigDecimal.TEN;

        StepVerifier
            .create(service.decreaseBalance(userId, amount))
            .expectError(InsufficientFundsException.class)
            .verify();

        verify(repository, times(1)).findByUserId(userId);
    }

    @Test
    void testAddBalanceNewBalance() {
        long userId = 13L;
        BigDecimal amount = BigDecimal.TEN;
        Balance newBalance = new Balance();
        newBalance.setBalance(amount);
        newBalance.setUserId(userId);
        when(repository.findByUserId(anyLong())).thenReturn(Mono.empty());
        when(repository.save(any(Balance.class))).thenReturn(Mono.just(newBalance));

        StepVerifier
            .create(service.addBalance(userId, amount))
            .assertNext(balance -> assertAll(
                () -> assertEquals(newBalance.getUserId(), balance.getUserId()),
                () -> assertEquals(newBalance.getBalance(), balance.getBalance())
            ))
            .verifyComplete();

        verify(repository, times(1)).findByUserId(userId);
        verify(repository, times(1)).save(balanceCaptor.capture());

        assertEquals(newBalance, balanceCaptor.getValue());
    }


    @Test
    void testAddBalanceExistingBalance() {
        long userId = 13L;
        BigDecimal amount = BigDecimal.TEN;

        Balance actualBalance = new Balance();
        actualBalance.setId(15L);
        actualBalance.setBalance(BigDecimal.TWO);
        actualBalance.setUserId(userId);

        Balance newBalance = new Balance();
        newBalance.setId(actualBalance.getId());
        newBalance.setUserId(actualBalance.getUserId());
        newBalance.setBalance(actualBalance.getBalance().add(amount));

        when(repository.findByUserId(anyLong())).thenReturn(Mono.just(actualBalance));
        when(repository.save(any(Balance.class))).thenReturn(Mono.just(newBalance));

        StepVerifier
            .create(service.addBalance(userId, amount))
            .assertNext(balance -> assertEquals(newBalance, balance))
            .verifyComplete();

        verify(repository, times(1)).findByUserId(userId);
        verify(repository, times(1)).save(balanceCaptor.capture());

        assertEquals(newBalance, balanceCaptor.getValue());
    }
}
