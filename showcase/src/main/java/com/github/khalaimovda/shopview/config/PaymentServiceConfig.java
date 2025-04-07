package com.github.khalaimovda.shopview.config;


import com.github.khalaimovda.showcase.api.DefaultApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentServiceConfig {

    @Bean
    public DefaultApi defaultApi() {
        return new DefaultApi();
    }
}
