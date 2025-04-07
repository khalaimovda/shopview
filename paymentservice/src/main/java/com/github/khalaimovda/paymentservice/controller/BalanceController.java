package com.github.khalaimovda.paymentservice.controller;


import com.github.khalaimovda.paymentservice.api.DefaultApi;
import com.github.khalaimovda.paymentservice.domain.Balance;
import com.github.khalaimovda.paymentservice.domain.PaymentRequest;
import com.github.khalaimovda.paymentservice.domain.PaymentSuccessResponse;
import com.github.khalaimovda.paymentservice.mapper.BalanceMapper;
import com.github.khalaimovda.paymentservice.service.BalanceService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
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
    public Mono<ResponseEntity<Balance>> apiBalanceGet(
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) {
        return service
            .getBalance()
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<PaymentSuccessResponse>> apiPaymentsPost(
        @Parameter(name = "PaymentRequest", description = "", required = true) @Valid @RequestBody Mono<PaymentRequest> paymentRequest,
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) {
        return paymentRequest
            .flatMap(pr -> service.decreaseBalance(pr.getAmount()))
            .map(mapper::toPaymentSuccessResponse)
            .map(ResponseEntity::ok);
    }
}