package com.kniemiec.soft.transferorchestrator.transfer.api;

import com.kniemiec.soft.transferorchestrator.MockData;
import com.kniemiec.soft.transferorchestrator.transfer.api.TransferStatusController;
import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferStatus;
import com.kniemiec.soft.transferorchestrator.transfer.services.TransferStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

class TransferStatusControllerTest {

    private TransferStatusService transferStatusService;

    private TransferStatusController underTest;

    @BeforeEach
    void setUp() {
        transferStatusService = Mockito.mock(TransferStatusService.class);
        underTest = new TransferStatusController(transferStatusService);
    }

    @Test
    void getTransferData() {
        UUID transferId = UUID.randomUUID();

        Mockito.when(transferStatusService.getTransferStatus(transferId)).thenReturn(Mono.just(MockData.mockTransferStatusData(transferId)));

        Mono<TransferStatus> transferStatusMono = underTest.getTransferData(transferId.toString());
        StepVerifier.create(transferStatusMono)
                .expectNextMatches(transferStatus -> transferStatus.getTransferId().equals(transferId)
                        && transferStatus.getStatus() == Status.CREATED)
                .verifyComplete();
    }

}