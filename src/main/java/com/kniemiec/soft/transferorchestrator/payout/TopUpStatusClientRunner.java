package com.kniemiec.soft.transferorchestrator.payout;

import com.kniemiec.soft.transferorchestrator.payout.model.TopUpStatusData;
import com.kniemiec.soft.transferorchestrator.transfer.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

//@Configuration
@Slf4j
//@Component
public class TopUpStatusClientRunner implements ApplicationRunner {

    WebClient payOutWebClient;

    Sinks.Many<TransferData> transferDataSink;

    DataTransferRepository dataTransferRepository;

    @Value("${payout.baseUrl}")
    String url;

    @Value("${payout.path.stream}")
    String streamPath;

    public TopUpStatusClientRunner(WebClient payOutWebClient,
                                   Sinks.Many<TransferData> transferDataSink,
                                   DataTransferRepository dataTransferRepository)
    {
        this.payOutWebClient = payOutWebClient;
        this.transferDataSink = transferDataSink;
        this.dataTransferRepository = dataTransferRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        payOutWebClient
                .get()
                .uri(url+streamPath)
                .retrieve()
                .bodyToFlux(TopUpStatusData.class)
                .log()
                .map(topUpStatusData -> {
                    TransferData transferData = dataTransferRepository.findById(topUpStatusData.getId()).block();
                    return transferData.withStatus(Status.COMPLETED);
                })
                .map( transferData -> dataTransferRepository.save(transferData).block())
                .subscribe(transferData -> transferDataSink.tryEmitNext(transferData));
    }
}
