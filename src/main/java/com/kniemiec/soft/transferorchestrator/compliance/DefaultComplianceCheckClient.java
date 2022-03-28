package com.kniemiec.soft.transferorchestrator.compliance;

import com.kniemiec.soft.transferorchestrator.transfer.model.User;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Configuration
public class DefaultComplianceCheckClient implements ComplianceCheckService {

    private final ComplianceCheckServiceGrpc.ComplianceCheckServiceStub nonBlockingStub;

//    @Value("${compliance.host}")
    String host = "localhost";

//    @Value("${compliance.port}")
    int port = 9999;

    ManagedChannel channel;

    public DefaultComplianceCheckClient(){
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        nonBlockingStub = ComplianceCheckServiceGrpc.newStub(channel);
    }

    @Override
    public void check(String transferId, User sender, User recipient) {
        ComplianceRequest request = ComplianceRequest.newBuilder()
                .setTransferId(transferId)
                .setSenderData(ComplianceRequest.UserData.newBuilder()
                        .setAddress(ComplianceRequest.Address.newBuilder()
                                .setCity(sender.getAddress().getCity())
                                .setCountry(sender.getAddress().getCountry())
                                .setPostalCode(sender.getAddress().getPostalCode())
                                .setStreet(sender.getAddress().getStreet())
                                .build())
                        .build())
                .setReceiverData(ComplianceRequest.UserData.newBuilder()
                        .build())
                .build();

        StreamObserver<ComplianceResponse> responseStreamObserver = new StreamObserver<>() {
            @Override
            public void onNext(ComplianceResponse value) {
                log.info("Response from compliance received for transfer {}", value.getTransferId());
            }

            @Override
            public void onError(Throwable t) {
                log.info("Error sending request to compliance: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                log.info("Sending request to compliance completed");
            }
        };
        nonBlockingStub.checkCompliance(request, responseStreamObserver);
    }
}
