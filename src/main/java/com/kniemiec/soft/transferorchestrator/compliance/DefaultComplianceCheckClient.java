package com.kniemiec.soft.transferorchestrator.compliance;

import com.kniemiec.soft.transferorchestrator.compliance.model.ComplianceRequest;
import com.kniemiec.soft.transferorchestrator.compliance.model.ComplianceResponse;
import com.kniemiec.soft.transferorchestrator.compliance.model.ComplianceStatus;
import com.kniemiec.soft.transferorchestrator.transfer.model.User;
import com.kniemiec.soft.transferorchestrator.transfer.ports.ComplianceCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class DefaultComplianceCheckClient implements ComplianceCheckService {

    @Value("${compliance.check.url}")
    String complianceCheckUrl;

    WebClient complianceWebClient;

    public DefaultComplianceCheckClient(WebClient complianceWebClient){
        this.complianceWebClient = complianceWebClient;
    }

    @Override
    public Mono<Boolean> check(String transferId, User sender, User recipient) {
        ComplianceRequest request = ComplianceRequest.builder()
                        .transferId(transferId)
                        .build();

        return complianceWebClient
                .put()
                .uri(complianceCheckUrl)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.warn("Status code is: {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap( responseMessage -> Mono.empty());
                })
                .bodyToMono(ComplianceResponse.class)
                .flatMap( complianceResponse -> Mono.just(complianceResponse.getStatus().equals(ComplianceStatus.OK)));
    }
}
