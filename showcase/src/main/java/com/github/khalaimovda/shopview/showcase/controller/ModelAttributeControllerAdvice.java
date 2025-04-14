package com.github.khalaimovda.shopview.showcase.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authorization-oriented expressions (such as those in 'sec:authorize') are restricted in WebFlux applications
 * due to a lack of support in the reactive side of Spring Security (as of Spring Security 5.1).
 * Only a minimal set of security expressions is allowed:
 * [isAuthenticated(), isFullyAuthenticated(), isAnonymous(), isRememberMe()]
 *
 * That's why we need to create manual methods to access authentication/authorization params
 */
@ControllerAdvice
public class ModelAttributeControllerAdvice {

    @ModelAttribute("authenticated")
    public Mono<Boolean> authenticated() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::isAuthenticated)
            .defaultIfEmpty(false);
    }

    @ModelAttribute("authorities")
    public Mono<List<String>> authorities() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(auth -> auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()))
            .defaultIfEmpty(Collections.emptyList());
    }
}
