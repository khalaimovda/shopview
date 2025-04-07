package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.exception.PaymentServiceException;
import com.github.khalaimovda.shopview.showcase.mapper.PaymentMapper;
import com.github.khalaimovda.shopview.showcase.api.DefaultApi;
import com.github.khalaimovda.shopview.showcase.domain.Balance;
import com.github.khalaimovda.shopview.showcase.domain.InsufficientFundsError;
import com.github.khalaimovda.shopview.showcase.domain.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @Override
    public Mono<Balance> getBalance() {
        return api
            .apiBalanceGet()
            .onErrorResume(
                WebClientResponseException.class,
                ex -> Mono.error(new PaymentServiceException(ex.getStatusCode(), ex.getMessage()))); // todo: Обработка ошибок
    }

    @Override
    public Mono<Balance> makePayment(BigDecimal amount) {
        return api
            .apiPaymentsPost(new PaymentRequest().amount(amount))
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
