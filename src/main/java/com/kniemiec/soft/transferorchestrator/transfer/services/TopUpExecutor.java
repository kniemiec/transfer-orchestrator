package com.kniemiec.soft.transferorchestrator.transfer.services;

import com.kniemiec.soft.transferorchestrator.payout.PayOut;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpStatus;
import com.kniemiec.soft.transferorchestrator.transfer.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.TransferInitializationFailedException;
import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TopUpExecutor {

    private final PayOut payOut;

    private final TransferProcessor transferProcessor;

    private final DataTransferRepository dataTransferRepository;

    public TopUpExecutor(TransferProcessor transferProcessor,
                         PayOut payOut,
                         DataTransferRepository dataTransferRepository){
        this.transferProcessor = transferProcessor;
        this.payOut = payOut;
        this.dataTransferRepository = dataTransferRepository;
        initializeTopUpFlow();
    }

    private void initializeTopUpFlow(){
        this.transferProcessor.exposeQueue()
                .filter( transferData -> transferData.getStatus().equals(Status.CAPTURED))
                .subscribe( capturedTransfer -> payOut.topUp(capturedTransfer)
                        .filter( response -> response.getStatus().equals(TopUpStatus.CREATED))
                        .switchIfEmpty( Mono.error(new TransferInitializationFailedException("Error while delivering transfer "+capturedTransfer.getTransferId())))
                        .map(newTransferData -> capturedTransfer.withStatus(Status.TOP_UP_STARTED))
                        .flatMap(dataTransferRepository::save)
                        .subscribe(transferProcessor::addToQueue)
                );
    }

}
