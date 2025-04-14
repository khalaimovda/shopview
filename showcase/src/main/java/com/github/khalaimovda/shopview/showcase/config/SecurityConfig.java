package com.github.khalaimovda.shopview.showcase.config;

import com.github.khalaimovda.shopview.showcase.model.UserRole;
import com.github.khalaimovda.shopview.showcase.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        RedirectServerAuthenticationSuccessHandler loginSuccessHandler =
            new RedirectServerAuthenticationSuccessHandler("/products");

        RedirectServerLogoutSuccessHandler logoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
        logoutSuccessHandler.setLogoutSuccessUrl(URI.create("/products"));

        return http
            .authorizeExchange(exchanges -> exchanges
//                .anyExchange().authenticated()
                .anyExchange().permitAll()
            )
            .httpBasic(withDefaults())
            .formLogin(form -> form.authenticationSuccessHandler(loginSuccessHandler))
            .logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler))
            .build();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(
        PasswordEncoder passwordEncoder,
        ReactiveUserDetailsService userDetailsService
    ) {
        var provider = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository
            .findByUsername(username)
            .map(user -> User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().stream().map(UserRole::name).toArray(String[]::new))
                .build())
            .switchIfEmpty(Mono.error(new UsernameNotFoundException(username)));
    }
}
