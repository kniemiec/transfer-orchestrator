package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.transfer.model.LockTransferMapping;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface LockTransferMappingRepository extends ReactiveMongoRepository<LockTransferMapping,String> {
}
