package com.kniemiec.soft.transferorchestrator.transfer;


import com.kniemiec.soft.transferorchestrator.MockData;
import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferCreationData;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

public class TransferControllerTest {

    private TransferController transferController;

    private Orchestrator orchestrator;

    @BeforeEach
    void setUp(){
        orchestrator = Mockito.mock(Orchestrator.class);
        transferController = new TransferController(orchestrator);
    }


    @Test
    public void callOrchestratorWhenDataValid(){
        UUID newTransferId = UUID.randomUUID();
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();

        Mockito.when(orchestrator.startTransfer(any())).thenReturn(Mono.just(newTransferId));

        Mono<UUID> transferIdResponse = transferController.startTransfer(transferCreationData);
        StepVerifier.create(transferIdResponse)
                        .expectNextMatches(transferId -> transferId.equals(newTransferId))
                                .verifyComplete();

    }

    @Test
    public void callOrchestratorWhenCheckingTransfer(){
        UUID transferId = UUID.randomUUID();

        Mockito.when(orchestrator.getTransferStatus(transferId)).thenReturn(Mono.just(MockData.mockTransferStatusData(transferId)));

        Mono<TransferStatus> transferStatusMono = transferController.getTransferData(transferId.toString());
        StepVerifier.create(transferStatusMono)
                        .expectNextMatches(transferStatus -> transferStatus.getTransferId().equals(transferId)
                        && transferStatus.getStatus() == Status.CREATED)
                                .verifyComplete();
    }
}
