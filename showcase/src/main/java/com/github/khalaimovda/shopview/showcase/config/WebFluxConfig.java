package com.github.khalaimovda.shopview.showcase.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
@EnableConfigurationProperties(ImageServiceProperties.class)
@RequiredArgsConstructor
public class WebFluxConfig implements WebFluxConfigurer {

    private final ImageServiceProperties properties;

    @Bean
    public RouterFunction<ServerResponse> imageRouter() {
        return RouterFunctions
            .route(GET(properties.getBaseUrl() + "**"),
                request -> {
                    String path = request.path().substring(properties.getBaseUrl().length());
                    return ServerResponse.ok()
                        .bodyValue(new FileSystemResource(properties.getUploadDir() + "/" + path));
                });
    }
}
