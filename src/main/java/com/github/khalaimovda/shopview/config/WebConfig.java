package com.github.khalaimovda.shopview.config;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableConfigurationProperties(ImageServiceProperties.class)
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ImageServiceProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(properties.getBaseUrl() + "**")
            .addResourceLocations("file:" + properties.getUploadDir() + "/");
    }
}
