package com.github.khalaimovda.shopview.paymentservice.controller;


import com.github.khalaimovda.shopview.paymentservice.domain.InsufficientFundsError;
import com.github.khalaimovda.shopview.paymentservice.exception.InsufficientFundsException;
import com.github.khalaimovda.shopview.paymentservice.exception.UserBalanceNotFoundException;
import com.github.khalaimovda.shopview.paymentservice.mapper.ErrorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class BalanceControllerAdvice {

    private final ErrorMapper errorMapper;

    @ExceptionHandler(InsufficientFundsException.class)
    public Mono<ResponseEntity<InsufficientFundsError>> handleInsufficientFundsException(InsufficientFundsException ex) {
        return Mono.just(ResponseEntity
            .badRequest()
            .body(errorMapper.toInsufficientFundsError(ex, "Not enough funds to complete transaction")));
    }

    @ExceptionHandler(UserBalanceNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUserBalanceNotFoundException(UserBalanceNotFoundException ex) {
        return Mono.just(ResponseEntity
            .badRequest()
            .body(Map.of("userId", ex.getUserId())));
    }
}
