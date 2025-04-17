package com.github.khalaimovda.shopview.showcase.security;

import reactor.core.publisher.Mono;

public interface AuthorizationService {
    Mono<String> getAccessToken();
}
