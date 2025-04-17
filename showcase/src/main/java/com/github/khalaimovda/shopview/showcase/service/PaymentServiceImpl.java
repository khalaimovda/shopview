package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.api.DefaultApi;
import com.github.khalaimovda.shopview.showcase.domain.AddBalanceRequest;
import com.github.khalaimovda.shopview.showcase.domain.Balance;
import com.github.khalaimovda.shopview.showcase.domain.InsufficientFundsError;
import com.github.khalaimovda.shopview.showcase.domain.PaymentRequest;
import com.github.khalaimovda.shopview.showcase.exception.PaymentServiceException;
import com.github.khalaimovda.shopview.showcase.mapper.PaymentMapper;
import com.github.khalaimovda.shopview.showcase.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final DefaultApi api;
    private final PaymentMapper mapper;
    private final AuthorizationService authorizationService;

    @Override
    @PreAuthorize("isAuthenticated() and #userId == principal.id")
    public Mono<Balance> getBalance(long userId) {
        return authorizationService
            .getAccessToken()
            .flatMap(accessToken -> api
                .apiBalanceGet("Bearer " + accessToken, userId))
            .onErrorResume(
                WebClientRequestException.class,
                ex -> Mono.error(new PaymentServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Payment service request problems: " + ex.getMessage())
                )
            )
            .onErrorResume(
                WebClientResponseException.class,
                ex -> Mono.error(new PaymentServiceException(ex.getStatusCode(), ex.getMessage())));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<Balance> addBalance(long userId, BigDecimal amount) {
        return authorizationService
            .getAccessToken()
            .flatMap(accessToken -> api
                .apiBalancePost("Bearer " + accessToken, new AddBalanceRequest().userId(userId).amount(amount)))
            .onErrorResume(
                WebClientRequestException.class,
                ex -> Mono.error(new PaymentServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Payment service request problems: " + ex.getMessage())
                )
            )
            .onErrorResume(
                WebClientResponseException.class,
                ex -> Mono.error(new PaymentServiceException(ex.getStatusCode(), ex.getMessage()))
            );
    }

    @Override
    @PreAuthorize("isAuthenticated() and #userId == principal.id")
    public Mono<Balance> makePayment(long userId, BigDecimal amount) {
        return authorizationService
            .getAccessToken()
            .flatMap(accessToken -> api
                .apiPaymentsPost("Bearer " + accessToken, new PaymentRequest().userId(userId).amount(amount)))
            .onErrorResume(
                WebClientRequestException.class,
                ex -> Mono.error(new PaymentServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Payment service request problems: " + ex.getMessage())
                )
            )
            .onErrorResume(WebClientResponseException.class, ex -> {
                if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    try {
                        InsufficientFundsError error = ex.getResponseBodyAs(InsufficientFundsError.class);
                        return Mono.error(mapper.toInsufficientFundsException(error, ex.getStatusCode()));
                    } catch (Exception ignored){ }
                }
                return Mono.error(new PaymentServiceException(ex.getStatusCode(), ex.getMessage()));
            })
            .map(mapper::toBalance);
    }
}
