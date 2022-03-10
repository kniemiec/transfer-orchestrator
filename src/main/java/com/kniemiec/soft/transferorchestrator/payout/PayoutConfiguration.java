package com.kniemiec.soft.transferorchestrator.payout;

import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Sinks;

@Configuration
@PropertySource("classpath:application.yaml")
public class PayoutConfiguration {


    @Bean(name = "payOutWebClient")
    public WebClient payoutWebClient(@Value("${payout.baseUrl}") String url){
        return WebClient.create(url);
    }

    @Bean
    public Sinks.Many<TransferData> getTransferDataSink(){
        return Sinks.many().replay().all();
    }

//    @Bean
//    public TopUpStatusClientRunner getTopUpClientApplicationRunner(WebClient payOutWebClient, Sinks.Many<TopUpStatusData> sink){
//        return new TopUpStatusClientRunner(payOutWebClient,sink);
//    }

}
