package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class TransferProcessor {

    private Sinks.Many<TransferData> transfers;

    public TransferProcessor(){
        transfers = Sinks.many().replay().latest();
    }

    public TransferData addToQueue(TransferData transferData){
        transfers.tryEmitNext(transferData);
        return transferData;
    }

    public Flux<TransferData> exposeQueue(){
        return transfers.asFlux();
    }

}
