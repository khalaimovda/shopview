package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.api.DefaultApi;
import com.github.khalaimovda.shopview.showcase.domain.Balance;
import com.github.khalaimovda.shopview.showcase.domain.PaymentRequest;
import com.github.khalaimovda.shopview.showcase.domain.PaymentSuccessResponse;
import com.github.khalaimovda.shopview.showcase.exception.PaymentServiceException;
import com.github.khalaimovda.shopview.showcase.mapper.PaymentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private DefaultApi api;

    @Spy
    private PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void testGetBalance() {
        Balance actualBalance = new Balance().balance(BigDecimal.TWO);
        when(api.apiBalanceGet()).thenReturn(Mono.just(actualBalance));

        StepVerifier
            .create(paymentService.getBalance())
            .assertNext(balance -> assertEquals(actualBalance, balance))
            .verifyComplete();

        verify(api, times(1)).apiBalanceGet();
    }

    @Test
    void testGetBalanceWebClientRequestException() throws URISyntaxException {
        when(api.apiBalanceGet()).thenReturn(Mono.error(new WebClientRequestException(
            new Throwable(), HttpMethod.GET, new URI("http://payment-service.com"), HttpHeaders.EMPTY)));

        StepVerifier
            .create(paymentService.getBalance())
            .expectError(PaymentServiceException.class)
            .verify();

        verify(api, times(1)).apiBalanceGet();
    }

    @Test
    void testGetBalanceWebClientResponseException() throws URISyntaxException {
        when(api.apiBalanceGet()).thenReturn(Mono.error(new WebClientResponseException(
            400, "Bad request", HttpHeaders.EMPTY, new byte[10], Charset.defaultCharset())));

        StepVerifier
            .create(paymentService.getBalance())
            .expectError(PaymentServiceException.class)
            .verify();

        verify(api, times(1)).apiBalanceGet();
    }


    @Test
    void testMakePayment() {
        Balance actualBalance = new Balance().balance(BigDecimal.TWO);
        BigDecimal amount = BigDecimal.ONE;
        PaymentRequest paymentRequest = new PaymentRequest().amount(amount);
        BigDecimal remainingAmount = actualBalance.getBalance().subtract(amount);
        Balance remainingBalance = new Balance().balance(remainingAmount);
        PaymentSuccessResponse paymentSuccessResponse = new PaymentSuccessResponse().balance(remainingAmount);

        when(api.apiPaymentsPost(any(PaymentRequest.class))).thenReturn(Mono.just(paymentSuccessResponse));

        StepVerifier
            .create(paymentService.makePayment(amount))
            .assertNext(balance -> assertEquals(remainingBalance, balance))
            .verifyComplete();

        verify(api, times(1)).apiPaymentsPost(paymentRequest);
    }

    @Test
    void testMakePaymentWebClientRequestException() throws URISyntaxException {
        BigDecimal amount = BigDecimal.ONE;
        PaymentRequest paymentRequest = new PaymentRequest().amount(amount);
        when(api.apiPaymentsPost(any(PaymentRequest.class))).thenReturn(Mono.error(new WebClientRequestException(
            new Throwable(), HttpMethod.POST, new URI("http://payment-service.com"), HttpHeaders.EMPTY)));

        StepVerifier
            .create(paymentService.makePayment(amount))
            .expectError(PaymentServiceException.class)
            .verify();

        verify(api, times(1)).apiPaymentsPost(paymentRequest);
    }

    @Test
    void testMakePaymentWebClientResponseException() {
        BigDecimal amount = BigDecimal.TWO;
        PaymentRequest paymentRequest = new PaymentRequest().amount(amount);
        when(api.apiPaymentsPost(any(PaymentRequest.class))).thenReturn(Mono.error(new WebClientResponseException(
            400, "Bad request", HttpHeaders.EMPTY, new byte[10], Charset.defaultCharset())));

        StepVerifier
            .create(paymentService.makePayment(amount))
            .expectError(PaymentServiceException.class)
            .verify();

        verify(api, times(1)).apiPaymentsPost(paymentRequest);
    }

}
