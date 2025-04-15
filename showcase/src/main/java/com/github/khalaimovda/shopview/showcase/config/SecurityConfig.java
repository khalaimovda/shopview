package com.github.khalaimovda.shopview.showcase.config;

import com.github.khalaimovda.shopview.showcase.repository.UserRepository;
import com.github.khalaimovda.shopview.showcase.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(ImageServiceProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final ImageServiceProperties imageProperties;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        RedirectServerAuthenticationSuccessHandler loginSuccessHandler =
            new RedirectServerAuthenticationSuccessHandler("/products");

        RedirectServerLogoutSuccessHandler logoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
        logoutSuccessHandler.setLogoutSuccessUrl(URI.create("/products"));

        return http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/js/**", "/css/**", imageProperties.getBaseUrl() + "**").permitAll()
                .pathMatchers(HttpMethod.GET, "/products", "/products/*").permitAll()
                .pathMatchers("/login").permitAll()
                .pathMatchers("/users").hasRole("ADMIN")
                .anyExchange().authenticated()
            )
//            .formLogin(form -> form.authenticationSuccessHandler(loginSuccessHandler))
//            .formLogin(form -> form
//                .authenticationSuccessHandler(loginSuccessHandler)
//                .authenticationFailureHandler(new RedirectServerAuthenticationFailureHandler("/login?error"))
//            )
//            .logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler))

            .formLogin(withDefaults())
            .logout(withDefaults())
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
            .map(user -> {
                List<? extends GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    .toList();
                return (UserDetails) new AuthenticatedUser(user.getId(), user.getUsername(), user.getPassword(), authorities);
            })
            .switchIfEmpty(Mono.error(new UsernameNotFoundException(username)));
    }
}
