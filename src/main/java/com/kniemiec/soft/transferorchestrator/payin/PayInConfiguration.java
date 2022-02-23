package com.kniemiec.soft.transferorchestrator.payin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@PropertySource("classpath:application.yaml")
public class PayInConfiguration {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public PayIn payIn(WebClient webClient){
        return new DefaultPayIn(webClient);
    }
}
