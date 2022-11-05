package com.kniemiec.soft.transferorchestrator;

import com.kniemiec.soft.transferorchestrator.confirmation.TopUpConfirmationServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class TransferConfirmationServiceRunner implements ApplicationRunner {

    private TopUpConfirmationServiceImpl topUpConfirmationService;

    private int port;

    public TransferConfirmationServiceRunner(TopUpConfirmationServiceImpl topUpConfirmationService,
                                             @Value("${grpc.server.port}") int port){
        this.topUpConfirmationService = topUpConfirmationService;
        this.port = port;
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception{
        Server server = ServerBuilder
                .forPort(port)
                .addService(topUpConfirmationService).build();

        server.start();
        server.awaitTermination();
    }
}
