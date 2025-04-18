package com.github.khalaimovda.shopview.paymentservice.integration;

import com.github.khalaimovda.shopview.paymentservice.domain.AddBalanceRequest;
import com.github.khalaimovda.shopview.paymentservice.domain.PaymentRequest;
import com.github.khalaimovda.shopview.paymentservice.repository.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class BalanceControllerIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private ReactiveJwtDecoder jwtDecoder;

    @Autowired
    private BalanceRepository balanceRepository;

    @BeforeEach
    public void setup() {
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwtRead));
    }

    @Test
    void testGetBalance_WithReadPermission_ReturnsOk() {
        webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwtRead))
            .get()
            .uri("/v1/api/balance?userId=1")
            .header("Authorization", "Bearer mock-token")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.balance").isEqualTo(300.0);
    }

    @Test
    void testGetBalance_WithoutAuthentication_ReturnsUnauthorized() {
        webTestClient
            .get()
            .uri("/v1/api/balance?userId=1")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testGetBalance_WithInsufficientPermission_ReturnsForbidden() {
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwtNoRoles));

        webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwtNoRoles))
            .get()
            .uri("/v1/api/balance?userId=1")
            .header("Authorization", "Bearer mock-token")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void testAddBalance_WithWritePermission_ReturnsOk() {
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwtWrite));

        AddBalanceRequest request = new AddBalanceRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("50.00"));

        webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwtWrite))
            .post()
            .uri("/v1/api/balance")
            .header("Authorization", "Bearer mock-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.balance").isEqualTo(350.0);

        StepVerifier.create(balanceRepository.findByUserId(1L))
            .expectNextMatches(balance ->
                balance.getBalance().compareTo(new BigDecimal("350.00")) == 0)
            .verifyComplete();
    }

    @Test
    void testAddBalance_WithReadPermission_ReturnsForbidden() {
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwtRead));

        AddBalanceRequest request = new AddBalanceRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("50.00"));

        webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwtRead))
            .post()
            .uri("/v1/api/balance")
            .header("Authorization", "Bearer mock-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isForbidden();

        StepVerifier.create(balanceRepository.findByUserId(1L))
            .expectNextMatches(balance ->
                balance.getBalance().compareTo(new BigDecimal("300.00")) == 0)
            .verifyComplete();
    }

    @Test
    void testMakePayment_WithWritePermission_ReturnsOk() {
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwtWrite));

        PaymentRequest request = new PaymentRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("100.00"));

        webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwtWrite))
            .post()
            .uri("/v1/api/payments")
            .header("Authorization", "Bearer mock-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.balance").isEqualTo(200.0);

        StepVerifier.create(balanceRepository.findByUserId(1L))
            .expectNextMatches(balance ->
                balance.getBalance().compareTo(new BigDecimal("200.00")) == 0)
            .verifyComplete();
    }

    @Test
    void testMakePayment_InsufficientFunds_ReturnsBadRequest() {
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwtWrite));

        PaymentRequest request = new PaymentRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("500.00"));

        webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwtWrite))
            .post()
            .uri("/v1/api/payments")
            .header("Authorization", "Bearer mock-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Not enough funds to complete transaction")
            .jsonPath("$.availableBalance").isEqualTo(300.0)
            .jsonPath("$.requiredAmount").isEqualTo(500.0);

        StepVerifier.create(balanceRepository.findByUserId(1L))
            .expectNextMatches(balance ->
                balance.getBalance().compareTo(new BigDecimal("300.00")) == 0)
            .verifyComplete();
    }
}