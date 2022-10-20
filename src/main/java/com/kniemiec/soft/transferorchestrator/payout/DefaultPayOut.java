package com.kniemiec.soft.transferorchestrator.payout;

import com.kniemiec.soft.transferorchestrator.payout.model.TopUpData;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpStatusData;
import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpResponse;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Slf4j
@Service
public class DefaultPayOut implements PayOut {

    @Value("${payout.baseUrl}")
    public String url;

    @Value("${payout.path.topup}")
    public String topUpPath;

    @Value("${payout.path.stream}")
    public String streamPath;

    private WebClient payOutWebClient;

    @Autowired
    public DefaultPayOut(WebClient payOutWebClient,
                         Sinks.Many<TransferData> sink){
        this.payOutWebClient = payOutWebClient;
    }

    @Override
    public Mono<TopUpResponse> topUp(TransferData transferData) {
        Money money = transferData.getMoney();
        String transferId = transferData.getTransferId();
        String senderId = transferData.getSenderId();
        String recipientId = transferData.getRecipientId();
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
