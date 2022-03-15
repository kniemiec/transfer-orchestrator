package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;


public interface DataTransferRepository extends ReactiveMongoRepository<TransferData,String> {

    Mono<TransferData> findByLockId(String lockId);
}
