package com.kniemiec.soft.transferorchestrator.payout;

import com.kniemiec.soft.transferorchestrator.payout.model.TopUpData;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpStatusData;
import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpResponse;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Slf4j
public class DefaultPayOut implements PayOut {

    @Value("${payout.baseUrl}")
    public String url;

    @Value("${payout.path.topup}")
    public String topUpPath;

    @Value("${payout.path.stream}")
    public String streamPath;

    WebClient payOutWebClient;


    public DefaultPayOut(WebClient webClient,
                         Sinks.Many<TransferData> sink){
        this.payOutWebClient = webClient;
    }

    @Override
    public Mono<TopUpResponse> topUp(Money money, String transferId, String senderId, String recipientId) {
        TopUpData topUpData = TopUpData.from(UUID.fromString(transferId), senderId, recipientId, money);
        return payOutWebClient
                .post()
                .uri(url+topUpPath)
                .bodyValue(topUpData)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.warn("Status code is: {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap( responseMessage -> Mono.just(null));
                })
                .bodyToMono(TopUpStatusData.class)
                .flatMap( topUpStatusData -> {
                    return Mono.just(TopUpResponse.from(topUpStatusData));
                });
    }
}
