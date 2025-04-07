package com.github.khalaimovda.paymentservice.controller;


import com.github.khalaimovda.paymentservice.domain.InsufficientFundsError;
import com.github.khalaimovda.paymentservice.exception.InsufficientFundsException;
import com.github.khalaimovda.paymentservice.mapper.ErrorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

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
}
