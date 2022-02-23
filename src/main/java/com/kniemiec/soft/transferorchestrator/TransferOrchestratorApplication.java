package com.kniemiec.soft.transferorchestrator;

import com.kniemiec.soft.transferorchestrator.transfer.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.model.Address;
import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootApplication
public class TransferOrchestratorApplication {

    private final DataTransferRepository repository;

    public TransferOrchestratorApplication(DataTransferRepository repository){
        this.repository = repository;
    }

//    @Override
//    public void run(String [] args){
//        this.repository
//                .deleteAll()
//                .thenMany(Flux.just(new TransferData(
//                                UUID.randomUUID().toString(),
//                                "sender1",
//                                "recipient1",
//                                new Address("1","2", "3", "4"),
//                                new Address("1","2", "3", "4"),
//                                new Money("PLN", BigDecimal.valueOf(100))
//                        ),new TransferData(
//                                UUID.randomUUID().toString(),
//                                "sender2",
//                                "recipient2",
//                                new Address("5","2", "3", "4"),
//                                new Address("7","2", "3", "4"),
//                                new Money("USD", BigDecimal.valueOf(100))
//                        ),new TransferData(
//                                UUID.randomUUID().toString(),
//                                "sender3",
//                                "recipient3",
//                                new Address("9","2", "3", "15"),
//                                new Address("10","2", "3", "15"),
//                                new Money("USD", BigDecimal.valueOf(100))
//                        ))
//                ).flatMap(repository::save)
//                .log()
//                .subscribe( null, null, () -> System.out.println("Initialization done"));
//    }

    public static void main(String[] args) {
        SpringApplication.run(TransferOrchestratorApplication.class, args);
    }

}
