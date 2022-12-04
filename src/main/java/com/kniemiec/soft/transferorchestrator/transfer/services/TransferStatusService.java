package com.kniemiec.soft.transferorchestrator.transfer.services;

import com.kniemiec.soft.transferorchestrator.transfer.persistence.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TransferStatusService {

    private DataTransferRepository dataTransferRepository;

    public TransferStatusService(DataTransferRepository dataTransferRepository){
        this.dataTransferRepository = dataTransferRepository;
    }

    public Mono<TransferStatus> getTransferStatus(UUID transferId) {
        Mono<TransferData> transferData = dataTransferRepository.findById(transferId.toString());
        return transferData.map(TransferStatus::from);
    }
}
