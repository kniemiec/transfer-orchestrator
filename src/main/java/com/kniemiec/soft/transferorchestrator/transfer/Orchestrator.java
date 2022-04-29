package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.compliance.ComplianceCheckService;
import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.payout.PayOut;
import com.kniemiec.soft.transferorchestrator.transfer.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class Orchestrator {

    private final PayIn payin;

    private final ComplianceCheckService complianceCheckService;

    private final TransferProcessor transferProcessor;

    private final DataTransferRepository dataTransferRepository;

    public Orchestrator(PayIn payin, ComplianceCheckService complianceCheckService, TransferProcessor transferProcessor,
                        DataTransferRepository dataTransferRepository) {
        this.payin = payin;
        this.complianceCheckService = complianceCheckService;
        this.transferProcessor = transferProcessor;
        this.dataTransferRepository = dataTransferRepository;

        this.transferProcessor.exposeQueue()
                .filter( transferData -> transferData.getStatus().equals(Status.LOCKED))
                .subscribe( lockedTransfer -> payin.capture(lockedTransfer.getLockId())
                        .switchIfEmpty( Mono.error(new TransferInitializationFailedException("Error while capturing transfer "+lockedTransfer.getTransferId())))
                        .map(newTransferData -> lockedTransfer.withStatus(Status.CAPTURED))
                        .flatMap(dataTransferRepository::save)
                        .subscribe(transferProcessor::addToQueue));

        this.transferProcessor.exposeQueue()
                .filter( transferData -> transferData.getStatus().equals(Status.CAPTURED))
                .subscribe( capturedTransfer -> complianceCheckService.check(capturedTransfer.getTransferId(), capturedTransfer.getSender(), capturedTransfer.getRecipient())
                        .switchIfEmpty( Mono.error(new TransferInitializationFailedException("Error while topping up transfer "+capturedTransfer.getTransferId())))
                        .map(newTransferData -> capturedTransfer.withStatus(Status.COMPLIANCHE_CHECK))
                        .flatMap(dataTransferRepository::save)
                        .subscribe(transferProcessor::addToQueue));
    }

    public Mono<UUID> startTransfer(TransferCreationData transferCreationData){
        UUID transferId = UUID.randomUUID();
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

    public Mono<TransferStatus> getTransferStatus(UUID transferId) {
        Mono<TransferData> transferData = dataTransferRepository.findById(transferId.toString());
        return transferData.map(TransferStatus::from);
    }

    public Flux<TransferData> getStreamOfData(){
        return transferProcessor.exposeQueue();
    }
}
