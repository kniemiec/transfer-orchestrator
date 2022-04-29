package com.kniemiec.soft.transferorchestrator.compliance;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.WeakHashMap;

@Configuration
public class ComplianceConfiguration {

    @Bean
    public WebClient complianceWebClient(WebClient.Builder builder){
        return builder.build();
    }
}
