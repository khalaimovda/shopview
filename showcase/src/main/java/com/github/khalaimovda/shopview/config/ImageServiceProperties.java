package com.github.khalaimovda.shopview.config;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.images")
@Data
@Validated
public class ImageServiceProperties {
    @NotNull
    private String uploadDir;
    @NotNull
    private String baseUrl;
}
