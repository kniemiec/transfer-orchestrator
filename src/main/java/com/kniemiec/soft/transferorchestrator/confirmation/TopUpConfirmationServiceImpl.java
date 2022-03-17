package com.kniemiec.soft.transferorchestrator.confirmation;

import com.kniemiec.soft.transferorchestrator.topup.TopUpConfirmationServiceGrpc;
import com.kniemiec.soft.transferorchestrator.topup.TopUpRequest;
import com.kniemiec.soft.transferorchestrator.topup.TopUpResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class TopUpConfirmationServiceImpl extends TopUpConfirmationServiceGrpc.TopUpConfirmationServiceImplBase {

    @Override
    public void topUpCompleted(TopUpRequest request, StreamObserver<TopUpResponse> responseObserver) {
        TopUpResponse topUpResponse = TopUpResponse.newBuilder()
                .setTransferId(request.getTransferId()).build();
        responseObserver.onNext(topUpResponse);
        responseObserver.onCompleted();
    }
}
