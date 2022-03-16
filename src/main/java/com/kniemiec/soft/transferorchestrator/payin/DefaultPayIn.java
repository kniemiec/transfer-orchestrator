package com.kniemiec.soft.transferorchestrator.payin;

import com.kniemiec.soft.transferorchestrator.payin.model.*;
import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@AllArgsConstructor
@Slf4j
@Service
public class DefaultPayIn implements PayIn {

    @Value("${payin.lock.url}")
    private String lockUrl;

    @Value("${payin.capture.url}")
    private String captureUrl;

    private WebClient payInWebClient;

    @Autowired
    public DefaultPayIn(WebClient payInWebClient){
        this.payInWebClient = payInWebClient;
    }

    @Override
    public Mono<LockResponse> lock(Money money, String senderId) {
        LockRequest lockRequest = LockRequest.from(money,senderId);
        return payInWebClient
                .post()
                .uri(lockUrl)
                .bodyValue(lockRequest)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.warn("PayIn lock failed. Status code is: {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap( responseMessage -> Mono.just(new Exception("Invalid request, statusCode: ")));
                })
                .bodyToMono(LockResponse.class);
    }

    @Override
    public Mono<CaptureResponse> capture(String lockId) {
        CaptureRequest captureRequest = CaptureRequest.from(lockId);
        return payInWebClient
                .post()
                .uri(captureUrl)
                .bodyValue(captureRequest)
                .retrieve()
                .bodyToMono(CaptureResponse.class);
    }
}
