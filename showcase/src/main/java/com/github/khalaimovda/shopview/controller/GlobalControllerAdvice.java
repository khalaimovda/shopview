package com.github.khalaimovda.shopview.controller;

import com.github.khalaimovda.shopview.exception.InsufficientFundsException;
import com.github.khalaimovda.shopview.exception.PaymentServiceException;
import com.github.khalaimovda.shopview.mapper.PaymentMapper;
import com.github.khalaimovda.showcase.domain.InsufficientFundsError;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final PaymentMapper paymentMapper;

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleValidationExceptions(WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
        return Mono.just(ResponseEntity.badRequest().body(errors));
    }

    @ExceptionHandler({NoSuchElementException.class, IllegalStateException.class})
    public Mono<ResponseEntity<String>> handleNoSuchElementException(Exception ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleInternalServerError(Exception ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public Mono<ResponseEntity<InsufficientFundsError>> handleInsufficientFundsException(InsufficientFundsException ex) {
        return Mono.just(ResponseEntity.badRequest().body(paymentMapper.toInsufficientFundsError(ex)));
    }

    @ExceptionHandler(PaymentServiceException.class)
    public Mono<ResponseEntity<String>> handlePaymentServiceException(PaymentServiceException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getReason()));
    }
}
