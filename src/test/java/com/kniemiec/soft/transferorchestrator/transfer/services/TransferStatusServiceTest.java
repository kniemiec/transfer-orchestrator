package com.kniemiec.soft.transferorchestrator.transfer.services;

import com.kniemiec.soft.transferorchestrator.MockData;
import com.kniemiec.soft.transferorchestrator.transfer.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TransferStatusServiceTest {

    TransferStatusService underTest;

    private DataTransferRepository dataTransferRepository;

    @BeforeEach
    void setUp() {
        dataTransferRepository = Mockito.mock(DataTransferRepository.class);

        underTest = new TransferStatusService(dataTransferRepository);
    }

    @Test
    void getTransferStatus() {
        // given
        UUID transferId = UUID.randomUUID();
        TransferData transferData  = MockData.mockTransferData(transferId).withStatus(Status.CAPTURED);

        when(dataTransferRepository.findById(transferId.toString())).thenReturn(Mono.just(transferData));

        // when
        var retrievedTransferData = underTest.getTransferStatus(transferId);

        // then
        StepVerifier.create(retrievedTransferData).assertNext(transferStatus -> {
            transferStatus.getStatus().equals(transferData.getStatus());
        })
                .verifyComplete();

    }
}