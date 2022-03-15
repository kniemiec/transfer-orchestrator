package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.transfer.model.TransferCreationData;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.UUID;


@RestController
@Slf4j
public class TransferController {

    private final Orchestrator transferOrchestrator;

    public TransferController(Orchestrator orchestrator){
        this.transferOrchestrator = orchestrator;
    }

    @PostMapping(value = "/v2/start-transfer")
    @ResponseStatus(HttpStatus.CREATED)
    Mono<UUID> startTransfer(@RequestBody @Valid TransferCreationData transferCreationData){
        log.info("Alternative start transfer: {}", transferCreationData);
        return transferOrchestrator.startTransfer(transferCreationData);
    }

    @GetMapping(value = "/transfer-status/{transferId}")
    Mono<TransferStatus> getTransferData(@PathVariable String transferId){
        return transferOrchestrator.getTransferStatus(UUID.fromString(transferId));
    }


    @GetMapping(value = "/v2/transfers/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    Flux<TransferStatus> getTransferData(){
        return transferOrchestrator.getStreamOfData()
                .map(TransferStatus::from);
    }
}
