package com.kniemiec.soft.transferorchestrator.transfer.services;

import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.transfer.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.TransferInitializationFailedException;
import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferCreationData;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class StartTransferAction {

    private final DataTransferRepository dataTransferRepository;

    private final PayIn payin;

    private final TransferProcessor transferProcessor;

    public StartTransferAction(DataTransferRepository dataTransferRepository,
                                    PayIn payIn,
                                    TransferProcessor transferProcessor){
        this.dataTransferRepository = dataTransferRepository;
        this.payin = payIn;
        this.transferProcessor = transferProcessor;

    }

    public Mono<UUID> tryStartTransfer(TransferCreationData transferCreationData, UUID transferId) {
        return dataTransferRepository
                .save(transferCreationData.toNewTransferData(transferId))
                .flatMap(transferData -> payin.lock(transferData.getMoney(), transferData.getSenderId())
                        .filter(lockResponse -> lockResponse.getStatus().equals(LockStatus.LOCKED))
                        .map(lockResponse -> transferData.withStatus(Status.LOCKED).withLockId(lockResponse.getLockId())))
                .flatMap(dataTransferRepository::save)
                .map(transferProcessor::addToQueue)
                .flatMap( saved ->
                        Mono.just(UUID.fromString(saved.getTransferId()))
                )
                .switchIfEmpty(Mono.error(new TransferInitializationFailedException("Unable to initialize transfer for sender: " + transferCreationData.getSenderId()))
                );
    }
}
