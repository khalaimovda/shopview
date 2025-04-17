package com.github.khalaimovda.shopview.paymentservice.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity security) {
        return security
            .authorizeExchange(requests -> requests
                .pathMatchers("/swagger-ui.html", "/webjars/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(serverSpec -> serverSpec
                .jwt(jwtSpec -> {
                    ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
                    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
                        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
                        Map<String, Object> paymentservice = (Map<String, Object>) resourceAccess.get("paymentservice");
                        List<String> roles = (List<String>) paymentservice.get("roles");
                        return Flux.fromIterable(roles)
                            .map(SimpleGrantedAuthority::new);
                    });

                    jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter);
                })
            )
            .build();
    }
}
