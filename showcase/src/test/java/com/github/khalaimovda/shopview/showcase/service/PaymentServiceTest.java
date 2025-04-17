package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.api.DefaultApi;
import com.github.khalaimovda.shopview.showcase.domain.Balance;
import com.github.khalaimovda.shopview.showcase.domain.PaymentRequest;
import com.github.khalaimovda.shopview.showcase.domain.PaymentSuccessResponse;
import com.github.khalaimovda.shopview.showcase.exception.PaymentServiceException;
import com.github.khalaimovda.shopview.showcase.mapper.PaymentMapper;
import com.github.khalaimovda.shopview.showcase.security.AuthorizationService;
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

    @Mock
    private AuthorizationService authorizationService;

    @Spy
    private PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void testGetBalance() {
        String accessToken = "test-access-token";
        String authorization = "Bearer " + accessToken;
        long userId = 13L;

        Balance actualBalance = new Balance().balance(BigDecimal.TWO);
        when(api.apiBalanceGet(anyString(), anyLong())).thenReturn(Mono.just(actualBalance));
        when(authorizationService.getAccessToken()).thenReturn(Mono.just(accessToken));

        StepVerifier
            .create(paymentService.getBalance(userId))
            .assertNext(balance -> assertEquals(actualBalance, balance))
            .verifyComplete();

        verify(api, times(1)).apiBalanceGet(authorization, userId);
    }

    @Test
    void testGetBalanceWebClientRequestException() throws URISyntaxException {
        String accessToken = "test-access-token";
        String authorization = "Bearer " + accessToken;
        long userId = 13L;

        when(api.apiBalanceGet(anyString(), anyLong())).thenReturn(Mono.error(new WebClientRequestException(
            new Throwable(), HttpMethod.GET, new URI("http://payment-service.com"), HttpHeaders.EMPTY)));
        when(authorizationService.getAccessToken()).thenReturn(Mono.just(accessToken));

        StepVerifier
            .create(paymentService.getBalance(userId))
            .expectError(PaymentServiceException.class)
            .verify();

        verify(api, times(1)).apiBalanceGet(authorization, userId);
    }

    @Test
    void testGetBalanceWebClientResponseException() {
        String accessToken = "test-access-token";
        String authorization = "Bearer " + accessToken;
        long userId = 13L;

        when(api.apiBalanceGet(anyString(), anyLong())).thenReturn(Mono.error(new WebClientResponseException(
            400, "Bad request", HttpHeaders.EMPTY, new byte[10], Charset.defaultCharset())));
        when(authorizationService.getAccessToken()).thenReturn(Mono.just(accessToken));

        StepVerifier
            .create(paymentService.getBalance(userId))
            .expectError(PaymentServiceException.class)
            .verify();

        verify(api, times(1)).apiBalanceGet(authorization, userId);
    }


    @Test
    void testMakePayment() {
        String accessToken = "test-access-token";
        String authorization = "Bearer " + accessToken;
        long userId = 13L;

        Balance actualBalance = new Balance().balance(BigDecimal.TWO);
        BigDecimal amount = BigDecimal.ONE;
        PaymentRequest paymentRequest = new PaymentRequest().amount(amount).userId(userId);
        BigDecimal remainingAmount = actualBalance.getBalance().subtract(amount);
        Balance remainingBalance = new Balance().balance(remainingAmount);
        PaymentSuccessResponse paymentSuccessResponse = new PaymentSuccessResponse().balance(remainingAmount);

        when(api.apiPaymentsPost(anyString(), any(PaymentRequest.class))).thenReturn(Mono.just(paymentSuccessResponse));
        when(authorizationService.getAccessToken()).thenReturn(Mono.just(accessToken));

        StepVerifier
            .create(paymentService.makePayment(userId, amount))
            .assertNext(balance -> assertEquals(remainingBalance, balance))
            .verifyComplete();

        verify(api, times(1)).apiPaymentsPost(authorization, paymentRequest);
    }

    @Test
    void testMakePaymentWebClientRequestException() throws URISyntaxException {
        String accessToken = "test-access-token";
        String authorization = "Bearer " + accessToken;
        long userId = 13L;

        BigDecimal amount = BigDecimal.ONE;
        PaymentRequest paymentRequest = new PaymentRequest().amount(amount).userId(userId);
        when(api.apiPaymentsPost(anyString(), any(PaymentRequest.class))).thenReturn(Mono.error(new WebClientRequestException(
            new Throwable(), HttpMethod.POST, new URI("http://payment-service.com"), HttpHeaders.EMPTY)));
        when(authorizationService.getAccessToken()).thenReturn(Mono.just(accessToken));

        StepVerifier
            .create(paymentService.makePayment(userId, amount))
            .expectError(PaymentServiceException.class)
            .verify();

        verify(api, times(1)).apiPaymentsPost(authorization, paymentRequest);
    }

    @Test
    void testMakePaymentWebClientResponseException() {
        String accessToken = "test-access-token";
        String authorization = "Bearer " + accessToken;
        long userId = 13L;

        BigDecimal amount = BigDecimal.TWO;
        PaymentRequest paymentRequest = new PaymentRequest().amount(amount).userId(userId);
        when(api.apiPaymentsPost(anyString(), any(PaymentRequest.class))).thenReturn(Mono.error(new WebClientResponseException(
            400, "Bad request", HttpHeaders.EMPTY, new byte[10], Charset.defaultCharset())));
        when(authorizationService.getAccessToken()).thenReturn(Mono.just(accessToken));

        StepVerifier
            .create(paymentService.makePayment(userId, amount))
            .expectError(PaymentServiceException.class)
            .verify();

        verify(api, times(1)).apiPaymentsPost(authorization, paymentRequest);
    }

}
