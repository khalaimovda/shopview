package com.github.khalaimovda.shopview.paymentservice.controller;


import com.github.khalaimovda.shopview.paymentservice.api.DefaultApi;
import com.github.khalaimovda.shopview.paymentservice.domain.Balance;
import com.github.khalaimovda.shopview.paymentservice.domain.PaymentRequest;
import com.github.khalaimovda.shopview.paymentservice.domain.PaymentSuccessResponse;
import com.github.khalaimovda.shopview.paymentservice.mapper.BalanceMapper;
import com.github.khalaimovda.shopview.paymentservice.service.BalanceService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        @NotNull @Parameter(name = "userId", description = "ID of the user", required = true, in = ParameterIn.QUERY) @Valid @RequestParam(value = "userId", required = true) Long userId,
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) {
        return service
            .getBalance(userId)
            .map(mapper::toDomainBalance)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<PaymentSuccessResponse>> apiPaymentsPost(
        @Parameter(name = "PaymentRequest", description = "", required = true) @Valid @RequestBody Mono<PaymentRequest> paymentRequest,
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) {
        return paymentRequest
            .flatMap(pr -> service.decreaseBalance(pr.getUserId(), pr.getAmount()))
            .map(mapper::toPaymentSuccessResponse)
            .map(ResponseEntity::ok);
    }
}