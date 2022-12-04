package com.kniemiec.soft.transferorchestrator.transfer.api;

import com.kniemiec.soft.transferorchestrator.transfer.model.TransferStatus;
import com.kniemiec.soft.transferorchestrator.transfer.services.TransferStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class TransferStatusController {

    private final TransferStatusService transferStatusService;

    public TransferStatusController(TransferStatusService transferStatusService){
        this.transferStatusService = transferStatusService;
    }

    @GetMapping(value = "/v1/transfer-status/{transferId}")
    Mono<TransferStatus> getTransferData(@PathVariable String transferId){
        return transferStatusService.getTransferStatus(UUID.fromString(transferId));
    }
}
