package com.github.khalaimovda.shopview.paymentservice.controller;

import com.github.khalaimovda.shopview.paymentservice.domain.Balance;
import com.github.khalaimovda.shopview.paymentservice.domain.PaymentRequest;
import com.github.khalaimovda.shopview.paymentservice.exception.InsufficientFundsException;
import com.github.khalaimovda.shopview.paymentservice.mapper.BalanceMapperImpl;
import com.github.khalaimovda.shopview.paymentservice.mapper.ErrorMapperImpl;
import com.github.khalaimovda.shopview.paymentservice.service.BalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;


@WebFluxTest(BalanceController.class)
@Import({BalanceMapperImpl.class, ErrorMapperImpl.class})
public class BalanceControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BalanceService balanceService;

    @Test
    void testGetBalance() {
        Balance actualBalance = new Balance().balance(BigDecimal.TEN);
        when(balanceService.getBalance()).thenReturn(Mono.just(actualBalance));

        webTestClient
            .get()
            .uri("/v1/api/balance")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .json("{ \"balance\": 10.0 }");

        verify(balanceService, times(1)).getBalance();
    }

    @Test
    void testPayments() {
        BigDecimal amount = BigDecimal.TWO;
        PaymentRequest paymentRequest = new PaymentRequest().amount(amount);
        Balance actualBalance = new Balance().balance(BigDecimal.TEN);
        Balance newBalance = new Balance().balance(actualBalance.getBalance().subtract(amount));
        when(balanceService.decreaseBalance(any(BigDecimal.class))).thenReturn(Mono.just(newBalance));

        webTestClient
            .post()
            .uri("/v1/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(paymentRequest)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .json("{ \"balance\": 8.0 }");

        verify(balanceService, times(1)).decreaseBalance(amount);
    }

    @Test
    void testPaymentsInsufficientFunds() {
        BigDecimal amount = BigDecimal.TEN;
        PaymentRequest paymentRequest = new PaymentRequest().amount(amount);
        Balance actualBalance = new Balance().balance(BigDecimal.TWO);
        when(balanceService.decreaseBalance(any(BigDecimal.class))).thenReturn(Mono.error(
            new InsufficientFundsException(amount, actualBalance.getBalance())));

        webTestClient
            .post()
            .uri("/v1/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(paymentRequest)
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .json("""
                {
                    "error": "Not enough funds to complete transaction",
                    "requiredAmount": 10.0,
                    "availableBalance": 2.0
                }
                """);

        verify(balanceService, times(1)).decreaseBalance(amount);
    }
}
