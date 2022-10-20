package com.kniemiec.soft.transferorchestrator.transfer.services;

import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payin.model.CaptureStatus;
import com.kniemiec.soft.transferorchestrator.transfer.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.TransferInitializationFailedException;
import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CaptureExecutor {

    private final DataTransferRepository dataTransferRepository;

    private final PayIn payin;

    private final TransferProcessor transferProcessor;

    public CaptureExecutor(DataTransferRepository dataTransferRepository,
                           PayIn payIn,
                           TransferProcessor transferProcessor){
        this.dataTransferRepository = dataTransferRepository;
        this.payin = payIn;
        this.transferProcessor = transferProcessor;
        initializeCaptureFlow();
    }


    private void initializeCaptureFlow(){
        this.transferProcessor.exposeQueue()
                .log()
                .filter( transferData -> transferData.getStatus().equals(Status.COMPLIANCE_OK))
                .log()
                .subscribe( complianceVerifiedTransfer -> payin.capture(complianceVerifiedTransfer.getLockId())
                        .log()
                        .filter( captureResponse -> captureResponse.getStatus().equals(CaptureStatus.CAPTURED))
                        .log()
                        .switchIfEmpty( Mono.error(new TransferInitializationFailedException("Error while capturing transfer "+complianceVerifiedTransfer.getTransferId())))
                        .map(newTransferData -> complianceVerifiedTransfer.withStatus(Status.CAPTURED))
                        .log()
                        .flatMap(dataTransferRepository::save)
                                .subscribe( element -> System.out.println("element: "+element.getTransferId())));
//                        .subscribe(transferProcessor::addToQueue));
    }


}
