package com.kniemiec.soft.transferorchestrator.payin;

import com.kniemiec.soft.transferorchestrator.payin.model.*;
import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    public DefaultPayIn(WebClient webClient){
        payInWebClient = webClient;
    }

    @Override
    public Mono<LockResponse> lock(Money money, String senderId) {
        LockRequest lockRequest = LockRequest.from(money,senderId);
        return payInWebClient
                .post()
                .uri(lockUrl)
                .bodyValue(lockRequest)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap( response -> {
                    LockStatus lockStatus = LockStatus.REJECTED;
                    if (response.equals("OK")) {
                        lockStatus = LockStatus.LOCKED;
                    }
                    return Mono.just(new LockResponse(senderId,money,lockStatus));
                });
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
