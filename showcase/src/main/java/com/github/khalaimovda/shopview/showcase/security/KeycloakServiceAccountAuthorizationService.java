package com.github.khalaimovda.shopview.showcase.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class KeycloakServiceAccountAuthorizationService implements AuthorizationService {

    private final ReactiveOAuth2AuthorizedClientManager manager;

    @Override
    public Mono<String> getAccessToken() {
        return manager.authorize(OAuth2AuthorizeRequest
                .withClientRegistrationId("showcase")
                .principal("system")
                .build())
            .map(OAuth2AuthorizedClient::getAccessToken)
            .map(OAuth2AccessToken::getTokenValue);
    }
}
