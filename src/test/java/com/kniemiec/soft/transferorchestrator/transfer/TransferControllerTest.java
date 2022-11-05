package com.kniemiec.soft.transferorchestrator.transfer;


import com.kniemiec.soft.transferorchestrator.MockData;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferCreationData;
import com.kniemiec.soft.transferorchestrator.transfer.services.StartTransferExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

public class TransferControllerTest {

    private TransferController transferController;

    private StartTransferExecutor startTransferExecutor;

    @BeforeEach
    void setUp(){
        startTransferExecutor = Mockito.mock(StartTransferExecutor.class);
        transferController = new TransferController(startTransferExecutor,
                new TransferProcessor());
    }


    @Test
    public void callOrchestratorWhenDataValid(){
        UUID newTransferId = UUID.randomUUID();
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();

        Mockito.when(startTransferExecutor.tryStartTransfer(any())).thenReturn(Mono.just(newTransferId));

        Mono<UUID> transferIdResponse = transferController.startTransfer(transferCreationData);
        StepVerifier.create(transferIdResponse)
                        .expectNextMatches(transferId -> transferId.equals(newTransferId))
                                .verifyComplete();

    }

}
