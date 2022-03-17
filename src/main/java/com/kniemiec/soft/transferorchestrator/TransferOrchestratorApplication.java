package com.kniemiec.soft.transferorchestrator;

import com.kniemiec.soft.transferorchestrator.confirmation.TopUpConfirmationServiceImpl;
import com.kniemiec.soft.transferorchestrator.transfer.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class TransferOrchestratorApplication {

    public static void main(String[] args){
        SpringApplication.run(TransferOrchestratorApplication.class, args);
    }
}
