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

    private PayIn payin;

    private PayOut payout;

    private PayInProcessor payInProcessor;

    private TransferProcessor transferProcessor;

    private DataTransferRepository dataTransferRepository;

    public Orchestrator(PayIn payin, PayOut payout, TransferProcessor transferProcessor, PayInProcessor payInProcessor,
                        DataTransferRepository dataTransferRepository) {
        this.payin = payin;
        this.payout = payout;
        this.transferProcessor = transferProcessor;
        this.payInProcessor = payInProcessor;
        this.dataTransferRepository = dataTransferRepository;

//        this.transferProcessor.exposeQueue().subscribe(dataTransferRepository::save);

        this.payInProcessor.exposeLockQueue().subscribe(lockResponse -> {
            log.info("handling lockId: {}",lockResponse.getLockId());
            payin.capture(lockResponse.getLockId())
                    .map( captureResponse -> {
                        payInProcessor.addToQueue(captureResponse);
                        return captureResponse;
                    })
                    .flatMap(captureResponse -> dataTransferRepository.findByLockId(captureResponse.getLockId())
                            .map(transferData -> transferData.withStatus(Status.CAPTURED))
                    )
                    .flatMap(transferData -> dataTransferRepository.save(transferData))
                    .subscribe(dataTransfer -> transferProcessor.addToQueue(dataTransfer));
        });

        this.payInProcessor.exposeCaptureQueue().subscribe(captureResponse -> {
            log.info("handling capture for lockId: {}", captureResponse.getLockId());
            log.info("it means sending topup somewhere.");
        });
    }

    public Mono<UUID> startTransfer(TransferCreationData transferCreationData){
        UUID transferId = UUID.randomUUID();
        return dataTransferRepository
                .save(transferCreationData.toNewTransferData(transferId))
                .flatMap( transferData -> payin.lock(transferData.getMoney(), transferData.getSenderId())
                        .log()
                        .flatMap(lockResponse -> {
                            if (lockResponse.getStatus().equals(LockStatus.LOCKED)) {
                                log.info("received lockId: {}",lockResponse.getLockId());
                                payInProcessor.addToQueue(lockResponse);
                                TransferData newTransferData = transferData.withStatus(Status.LOCKED).withLockId(lockResponse.getLockId());
                                dataTransferRepository.save(newTransferData).subscribe(transferProcessor::addToQueue);
                                return Mono.just(UUID.fromString(newTransferData.getTransferId()));
                            } else {
                                return Mono.error(new TransferInitializationFailedException("Unable to initialize transfer for sender: " + transferData.getSenderId()));
                            }
                        }));
    }

    public Mono<TransferStatus> getTransferStatus(UUID transferId) {
        Mono<TransferData> transferData = dataTransferRepository.findById(transferId.toString());
        return transferData.map(transferData1 -> TransferStatus.from(transferData1));
    }

    public Flux<TransferData> getStreamOfData(){
        return transferProcessor.exposeQueue();
    }
}
