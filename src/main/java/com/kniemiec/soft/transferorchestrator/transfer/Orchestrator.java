package com.kniemiec.soft.transferorchestrator.transfer;

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

    private final PayOut payout;

    private final PayInProcessor payInProcessor;

    private final TransferProcessor transferProcessor;

    private final DataTransferRepository dataTransferRepository;

    public Orchestrator(PayIn payin, PayOut payout, TransferProcessor transferProcessor, PayInProcessor payInProcessor,
                        DataTransferRepository dataTransferRepository) {
        this.payin = payin;
        this.payout = payout;
        this.transferProcessor = transferProcessor;
        this.payInProcessor = payInProcessor;
        this.dataTransferRepository = dataTransferRepository;

        this.payInProcessor.exposeLockQueue().subscribe(lockResponse -> {
            log.info("handling Capture command for lockId: {}",lockResponse.getLockId());
            payin.capture(lockResponse.getLockId())
                    .map(payInProcessor::addToQueue)
                    .flatMap(captureResponse -> dataTransferRepository.findByLockId(captureResponse.getLockId()))
                    .map(transferData -> transferData.withStatus(Status.CAPTURED))
                    .flatMap(dataTransferRepository::save)
                    .subscribe(transferProcessor::addToQueue);
        });

        this.payInProcessor.exposeCaptureQueue().subscribe(captureResponse -> {
            log.info("handling capture for lockId: {}", captureResponse.getLockId());
            log.info("it means sending topup somewhere.");
        });
    }

    public Mono<UUID> startTransfer(TransferCreationData transferCreationData){
        UUID transferId = UUID.randomUUID();
        return dataTransferRepository
                .insert(transferCreationData.toNewTransferData(transferId))
                .flatMap(transferData -> payin.lock(transferData.getMoney(), transferData.getSenderId())
                        .filter(lockResponse -> lockResponse.getStatus().equals(LockStatus.LOCKED))
                        .map(payInProcessor::addToQueue)
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
