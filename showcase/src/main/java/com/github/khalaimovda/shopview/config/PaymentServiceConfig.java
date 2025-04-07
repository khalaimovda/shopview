package com.github.khalaimovda.shopview.config;


import com.github.khalaimovda.showcase.ApiClient;
import com.github.khalaimovda.showcase.api.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentServiceConfig {

    @Bean
    public ApiClient apiClient(@Value("${app.payment-service.url}") String paymentServiceUrl) {
        return new ApiClient().setBasePath(paymentServiceUrl);
    }

    @Bean
    public DefaultApi defaultApi(ApiClient apiClient) {
        return new DefaultApi(apiClient);
    }
}
