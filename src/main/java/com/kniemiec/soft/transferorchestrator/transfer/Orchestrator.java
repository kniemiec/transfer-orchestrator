package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.compliance.ComplianceCheckService;
import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.transfer.model.*;
import com.kniemiec.soft.transferorchestrator.transfer.services.StartTransferAction;
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

    private final StartTransferAction startTransferAction;

    public Orchestrator(PayIn payin, ComplianceCheckService complianceCheckService, TransferProcessor transferProcessor,
                        DataTransferRepository dataTransferRepository,
                        StartTransferAction startTransferAction) {
        this.payin = payin;
        this.complianceCheckService = complianceCheckService;
        this.transferProcessor = transferProcessor;
        this.dataTransferRepository = dataTransferRepository;
        this.startTransferAction = startTransferAction;

        this.transferProcessor.exposeQueue()
                .filter( transferData -> transferData.getStatus().equals(Status.COMPLIANCE_OK))
                .subscribe( complianceVerifiedTransfer -> payin.capture(complianceVerifiedTransfer.getLockId())
                        .switchIfEmpty( Mono.error(new TransferInitializationFailedException("Error while capturing transfer "+complianceVerifiedTransfer.getTransferId())))
                        .map(newTransferData -> complianceVerifiedTransfer.withStatus(Status.CAPTURED))
                        .flatMap(dataTransferRepository::save)
                        .subscribe(transferProcessor::addToQueue));

        this.transferProcessor.exposeQueue()
                .filter( transferData -> transferData.getStatus().equals(Status.LOCKED))
                .subscribe( lockedTransfer -> complianceCheckService.check(lockedTransfer.getTransferId(), lockedTransfer.getSender(), lockedTransfer.getRecipient())
//                        TODO - send this to DQL - DLQ needs to be created
//                        .switchIfEmpty( status->  dataTransferRepository.save(lockedTransfer.withStatus(Status.COMPLIANCE_ALERT))
                        .filter(complianceStatus -> complianceStatus)
                        .map(newTransferData -> lockedTransfer.withStatus(Status.COMPLIANCE_OK))
                        .flatMap(dataTransferRepository::save)
                        .subscribe(transferProcessor::addToQueue));
    }

    public Mono<UUID> startTransfer(TransferCreationData transferCreationData){
        UUID transferId = UUID.randomUUID();
        return startTransferAction.tryStartTransfer(transferCreationData, transferId);
    }

    public Mono<TransferStatus> getTransferStatus(UUID transferId) {
        Mono<TransferData> transferData = dataTransferRepository.findById(transferId.toString());
        return transferData.map(TransferStatus::from);
    }

    public Flux<TransferData> getStreamOfData(){
        return transferProcessor.exposeQueue();
    }
}
