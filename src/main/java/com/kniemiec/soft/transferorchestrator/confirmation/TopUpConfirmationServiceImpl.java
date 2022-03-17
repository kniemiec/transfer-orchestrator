package com.kniemiec.soft.transferorchestrator.confirmation;

import com.kniemiec.soft.transferorchestrator.topup.TopUpConfirmationServiceGrpc;
import com.kniemiec.soft.transferorchestrator.topup.TopUpRequest;
import com.kniemiec.soft.transferorchestrator.topup.TopUpResponse;
import com.kniemiec.soft.transferorchestrator.transfer.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TopUpConfirmationServiceImpl extends TopUpConfirmationServiceGrpc.TopUpConfirmationServiceImplBase {

    private TransferProcessor transferProcessor;

    private DataTransferRepository dataTransferRepository;

    @Autowired
    public TopUpConfirmationServiceImpl(TransferProcessor transferProcessor, DataTransferRepository dataTransferRepository){
        super();
        this.transferProcessor = transferProcessor;
        this.dataTransferRepository = dataTransferRepository;
    }

    @Override
    public void topUpCompleted(TopUpRequest request, StreamObserver<TopUpResponse> responseObserver) {
        log.info("TopUpConfirmation received: {}",request.getTransferId());
        dataTransferRepository.findById(request.getTransferId())
                .switchIfEmpty( Mono.error(new Exception("Unable to receive confirmation")))
                .map(transferData -> transferData.withStatus(Status.COMPLETED))
                .flatMap(dataTransferRepository::save)
                .map(transferData -> {
                    log.info("TransferData saved: {}", transferData.getStatus());
                    return transferProcessor.addToQueue(transferData);
                })
                .map(transferData -> TopUpResponse.newBuilder()
                        .setTransferId(request.getTransferId()).build())
                .subscribe( response -> {
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                });
//        response.filter()
    }
}
