package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.transfer.model.TransferCreationData;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;
import java.util.UUID;


@RestController
public class TransferController {

    private static Logger logger = LoggerFactory.getLogger(TransferController.class);

    private Orchestrator transferOrchestrator;

    private Sinks.Many<TransferData> transferDataSink;

    public TransferController(Orchestrator orchestrator, Sinks.Many<TransferData> transferDataSink){
        this.transferOrchestrator = orchestrator;
        this.transferDataSink = transferDataSink;
    }

    @PostMapping(value = "/v2/start-transfer")
    @ResponseStatus(HttpStatus.CREATED)
    Mono<UUID> startTransferV2(@RequestBody @Valid TransferCreationData transferCreationData){
        logger.info("Alternative start transfer: {}", transferCreationData);
        return transferOrchestrator.startTransfer(transferCreationData);
    }

    @GetMapping(value = "/transfer-status/{transferId}")
    Mono<TransferStatus> getTransferData(@PathVariable String transferId){
        return transferOrchestrator.getTransferStatus(UUID.fromString(transferId));
    }


    @GetMapping(value = "/v1/transfers/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    Flux<TransferStatus> getTransferData(){
        return transferDataSink.asFlux()
                .map( topUpStatusData -> TransferStatus.from(topUpStatusData));
    }
}
