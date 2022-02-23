package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;


public interface DataTransferRepository extends ReactiveMongoRepository<TransferData,String> {

}
