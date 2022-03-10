package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.payout.PayOut;
import com.kniemiec.soft.transferorchestrator.transfer.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class Orchestrator {

    private PayIn payin;

    private PayOut payout;

    private PayInProcessor payInProcessor;

    private DataTransferRepository dataTransferRepository;

    public Orchestrator(PayIn payin, PayOut payout, DataTransferRepository dataTransferRepository, PayInProcessor payInProcessor) {
        this.payin = payin;
        this.payout = payout;
        this.dataTransferRepository = dataTransferRepository;
        this.payInProcessor = payInProcessor;
        this.payInProcessor.exposeFlux().subscribe( lockResponse -> {
            log.info("handling lockId: {}",lockResponse.getLockId());
            payin.capture(lockResponse.getLockId())
                    .log()
                    .flatMap(captureResponse -> dataTransferRepository.findByCaptureId(lockResponse.getLockId())
                            .map( transferData -> transferData.withStatus(Status.CAPTURED))
                    )
                    .subscribe(dataTransferRepository::save);
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
                                payInProcessor.tryEmitNext(lockResponse);
                                TransferData newTransferData = transferData.withStatus(Status.LOCKED);
                                return dataTransferRepository.save(newTransferData).flatMap( updatedData -> Mono.just(UUID.fromString(updatedData.getTransferId())));
                            } else {
                                return Mono.error(new TransferInitializationFailedException("Unable to initialize transfer for sender: " + transferData.getSenderId()));
                            }
                        }));
    }

    public Mono<TransferStatus> getTransferStatus(UUID transferId) {
        Mono<TransferData> transferData = dataTransferRepository.findById(transferId.toString());
        return transferData.map(transferData1 -> TransferStatus.from(transferData1));
    }
}
