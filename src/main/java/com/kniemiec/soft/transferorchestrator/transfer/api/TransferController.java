package com.kniemiec.soft.transferorchestrator.transfer.api;

import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferCreationData;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferStatus;
import com.kniemiec.soft.transferorchestrator.transfer.services.StartTransferExecutor;
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

    private final StartTransferExecutor startTransferExecutor;

    private final TransferProcessor transferProcessor;

    public TransferController(StartTransferExecutor startTransferExecutor,
                              TransferProcessor transferProcessor){
        this.startTransferExecutor = startTransferExecutor;
        this.transferProcessor = transferProcessor;
    }

    @PostMapping(value = "/v1/start-transfer")
    @ResponseStatus(HttpStatus.CREATED)
    Mono<UUID> startTransfer(@RequestBody @Valid TransferCreationData transferCreationData){
        log.info("Alternative start transfer: {}", transferCreationData);
        return startTransferExecutor.tryStartTransfer(transferCreationData);
    }

    @GetMapping(value = "/v1/transfers/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    Flux<TransferStatus> getTransferData(){
        return transferProcessor.exposeQueue().map(TransferStatus::from);
    }
}
