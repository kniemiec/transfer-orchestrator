package com.kniemiec.soft.transferorchestrator.transfer.services;

import com.kniemiec.soft.transferorchestrator.transfer.ports.ComplianceCheckService;
import com.kniemiec.soft.transferorchestrator.transfer.persistence.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationExecutor {

    private final DataTransferRepository dataTransferRepository;

    private final ComplianceCheckService complianceCheckService;

    private final TransferProcessor transferProcessor;

    public AuthorizationExecutor(DataTransferRepository dataTransferRepository,
                                 ComplianceCheckService complianceCheckService,
                                 TransferProcessor transferProcessor){
        this.dataTransferRepository = dataTransferRepository;
        this.complianceCheckService = complianceCheckService;
        this.transferProcessor = transferProcessor;
        initializeAuthorizationFlow();
    }


    public void initializeAuthorizationFlow(){
        this.transferProcessor.exposeQueue()
                .log()
                .filter( transferData -> transferData.getStatus().equals(Status.LOCKED))
                .log()
                .subscribe( lockedTransfer -> complianceCheckService.check(lockedTransfer.getTransferId(), lockedTransfer.getSender(), lockedTransfer.getRecipient())
//                        TODO - send this to DQL - DLQ needs to be created
//                        .switchIfEmpty( status->  dataTransferRepository.save(lockedTransfer.withStatus(Status.COMPLIANCE_ALERT))
                        .log()
                        .filter(complianceStatus -> complianceStatus)
                        .map(newTransferData -> lockedTransfer.withStatus(Status.COMPLIANCE_OK))
                        .flatMap(dataTransferRepository::save)
                        .subscribe(transferProcessor::addToQueue));
    }
}
