package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payin.PayOutClientException;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.payout.PayOut;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpStatus;
import com.kniemiec.soft.transferorchestrator.transfer.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Service
@Slf4j
public class Orchestrator {

    private PayIn payin;

    private PayOut payout;

    private DataTransferRepository dataTransferRepository;

    private Sinks.Many<UUID> uuidSink = Sinks.many().replay().all();

    public Orchestrator(PayIn payin, PayOut payout, DataTransferRepository dataTransferRepository) {
        this.payin = payin;
        this.payout = payout;
        this.dataTransferRepository = dataTransferRepository;

        uuidSink.asFlux().subscribe(
             id -> {
                    dataTransferRepository.findById(id.toString())
                            .subscribe( transferData -> {
                                payout.topUp(transferData.getMoney(),transferData.getTransferId(), transferData.getSenderId(),transferData.getRecipientId())
                                        .log()
                                        .subscribe( transferStatus -> log.info("Topup request sent for transfer: {}", id));
                                dataTransferRepository.save(transferData.withStatus(Status.TOP_UP_STARTED)).block();
                            });
                });
    }

    public Mono<UUID> alternativeStartTransfer(TransferCreationData transferCreationData){
        UUID transferId = UUID.randomUUID();
        Mono<UUID> data =  dataTransferRepository.save(transferCreationData.toNewTransferData(transferId))
                .flatMap( transferData -> {
                    return payin.lock(transferData.getMoney(), transferData.getSenderId())
                            .log()
                            .flatMap(lockResponse -> {
                                if (lockResponse.getStatus().equals(LockStatus.LOCKED)) {
                                    transferData.withStatus(Status.LOCKED);
                                    return dataTransferRepository.save(transferData).flatMap( updatedData -> Mono.just(UUID.fromString(updatedData.getTransferId())));
                                } else {
                                    return Mono.error(new TransferInitializationFailedException("Unable to initialize transfer for sender: " + transferData.getSenderId()));
                                }
                            });
                })
                .doOnNext(id -> uuidSink.tryEmitNext(id));
        return data;
    }

    public Mono<UUID> startTransfer(TransferCreationData transferCreationData) {
        UUID transferId = UUID.randomUUID();
        return payin.lock(transferCreationData.getMoney(), transferCreationData.getSenderId())
                .log()
                .flatMap(lockResponse -> {
                    if (lockResponse.getStatus().equals(LockStatus.LOCKED)) {
                        return dataTransferRepository
                                .save(transferCreationData.toNewTransferData(transferId))
                                .log()
                                .flatMap(transferData -> {
                                    return payout.topUp(transferData.getMoney(), transferData.getTransferId(), transferData.getSenderId(), transferData.getRecipientId())
                                            .log()
                                            .flatMap(topUpResponse -> {
                                                if(topUpResponse.getStatus().equals(TopUpStatus.CREATED)) {
                                                    return Mono.just(UUID.fromString(transferData.getTransferId()));
                                                } else {
                                                    return Mono.error(new TransferInitializationFailedException("Unable to initialize transfer for recipient: "+transferCreationData.getRecipientId()));
                                                }
                                            })
                                            .switchIfEmpty(Mono.error(new PayOutClientException("exception", HttpStatus.BAD_REQUEST.value())))
                                    ;
                                });
                    } else {
                        return Mono.error(new TransferInitializationFailedException("Unable to initialize transfer for sender: " + transferCreationData.getSenderId()));
                    }
                });
    }

    public Mono<TransferStatus> getTransferStatus(UUID transferId) {
        Mono<TransferData> transferData = dataTransferRepository.findById(transferId.toString());
        return transferData.map(transferData1 -> TransferStatus.from(transferData1));
    }
}
