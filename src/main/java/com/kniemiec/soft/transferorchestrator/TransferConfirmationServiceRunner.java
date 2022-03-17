package com.kniemiec.soft.transferorchestrator;

import com.kniemiec.soft.transferorchestrator.confirmation.TopUpConfirmationServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TransferConfirmationServiceRunner implements ApplicationRunner {

    private TopUpConfirmationServiceImpl topUpConfirmationService;

    public TransferConfirmationServiceRunner(TopUpConfirmationServiceImpl topUpConfirmationService){
        this.topUpConfirmationService = topUpConfirmationService;
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception{
        Server server = ServerBuilder
                .forPort(9090)
                .addService(topUpConfirmationService).build();

        server.start();
        server.awaitTermination();
    }
}
