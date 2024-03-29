package com.kniemiec.soft.transferorchestrator.payin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@PropertySource("classpath:application.yaml")
public class PayInConfiguration {

    @Bean
    public WebClient payInWebClient(WebClient.Builder builder) {
        return builder.build();
    }

}
