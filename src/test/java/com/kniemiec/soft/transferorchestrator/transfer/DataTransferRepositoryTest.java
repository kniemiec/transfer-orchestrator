package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.MockData;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import com.kniemiec.soft.transferorchestrator.transfer.persistence.DataTransferRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DirtiesContext
@DataMongoTest
@ActiveProfiles("test")
public class DataTransferRepositoryTest {

    @Autowired
    DataTransferRepository dataTransferRepository;

    @BeforeEach
    void setUp(){
        var transfers = List.of(
                MockData.mockTransferData(),
                MockData.mockTransferData()
        );
        dataTransferRepository.saveAll(transfers).blockLast();
    }

    @AfterEach
    void tearDown() {
        dataTransferRepository.deleteAll().block();
    }

    @Test
    void findAllTest(){
        //given

        //when
        var transferDataFlux = dataTransferRepository.findAll().log();

        //then
        StepVerifier.create(transferDataFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findById(){
        //given
        UUID newTransferId = UUID.randomUUID();

        TransferData newTransferData = MockData.mockTransferData(newTransferId);
        dataTransferRepository.save(newTransferData).block();

        //when
        var newTransferFound = dataTransferRepository.findById(newTransferId.toString());

        //then
        StepVerifier.create(newTransferFound)
                .assertNext(transferData -> {
                    assertEquals(newTransferData.getSenderId(), transferData.getSenderId());
                    assertEquals(newTransferData.getRecipientId(), transferData.getRecipientId());
                })
                .verifyComplete();
    }
}
