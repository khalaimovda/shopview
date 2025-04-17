package com.github.khalaimovda.shopview.paymentservice.controller;


import com.github.khalaimovda.shopview.paymentservice.api.DefaultApi;
import com.github.khalaimovda.shopview.paymentservice.domain.AddBalanceRequest;
import com.github.khalaimovda.shopview.paymentservice.domain.Balance;
import com.github.khalaimovda.shopview.paymentservice.domain.PaymentRequest;
import com.github.khalaimovda.shopview.paymentservice.domain.PaymentSuccessResponse;
import com.github.khalaimovda.shopview.paymentservice.mapper.BalanceMapper;
import com.github.khalaimovda.shopview.paymentservice.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("${openapi.autoWorkshopManagement.base-path:/v1}")
@RequiredArgsConstructor
public class BalanceController implements DefaultApi {

    private final BalanceService service;
    private final BalanceMapper mapper;

    @Override
    @PreAuthorize("isAuthenticated() and hasAuthority('read')")
    public Mono<ResponseEntity<Balance>> apiBalanceGet(String authorization, Long userId, ServerWebExchange exchange) {
        return service
            .getBalance(userId)
            .map(mapper::toDomainBalance)
            .map(ResponseEntity::ok);
    }

    @Override
    @PreAuthorize("isAuthenticated() and hasAuthority('write')")
    public Mono<ResponseEntity<Balance>> apiBalancePost(String authorization, Mono<AddBalanceRequest> addBalanceRequest, ServerWebExchange exchange) {
        return addBalanceRequest
            .flatMap(abr -> service.addBalance(abr.getUserId(), abr.getAmount()))
            .map(mapper::toDomainBalance)
            .map(ResponseEntity::ok);
    }

    @Override
    @PreAuthorize("isAuthenticated() and hasAuthority('write')")
    public Mono<ResponseEntity<PaymentSuccessResponse>> apiPaymentsPost(String authorization, Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest
            .flatMap(pr -> service.decreaseBalance(pr.getUserId(), pr.getAmount()))
            .map(mapper::toPaymentSuccessResponse)
            .map(ResponseEntity::ok);
    }
}