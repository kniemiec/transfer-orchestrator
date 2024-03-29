package com.kniemiec.soft.transferorchestrator.transfer.services;

import com.kniemiec.soft.transferorchestrator.MockData;
import com.kniemiec.soft.transferorchestrator.payin.DefaultPayIn;
import com.kniemiec.soft.transferorchestrator.transfer.ports.PayIn;
import com.kniemiec.soft.transferorchestrator.payin.model.LockResponse;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.transfer.persistence.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferCreationData;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class StartTransferActionTest {

    private StartTransferExecutor underTest;

    private DataTransferRepository dataTransferRepositoryMock;

    private PayIn payInMock;

    private TransferProcessor transferProcessor;

    private TransferIdGenerator transferIdGenerator;

    @BeforeEach
    public void setUp(){
        dataTransferRepositoryMock = Mockito.mock(DataTransferRepository.class);
        payInMock = Mockito.mock(DefaultPayIn.class);
        transferProcessor = new TransferProcessor();
        transferIdGenerator = new DefaultTransferIdGenerator();
        underTest = new StartTransferExecutor(
            dataTransferRepositoryMock,
                payInMock,
                transferProcessor,
                transferIdGenerator
        );
    }


    @Test
    void tryStartTransfer() {
        // given
        UUID transferId = UUID.randomUUID();
        UUID lockId = UUID.randomUUID();
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        TransferData transferData = MockData.mockTransferData(transferId);
        LockResponse lockResponse = MockData.mockLockResponseData(lockId.toString(), LockStatus.LOCKED);

        when(dataTransferRepositoryMock.save(any(TransferData.class))).thenReturn(Mono.just(transferData));
        when(payInMock.lock(any(Money.class), any())).thenReturn(Mono.just(lockResponse));

        // when
        var result = underTest.tryStartTransfer(transferCreationData);

        // then
        StepVerifier.create(result)
                .expectNext(transferId)
                .verifyComplete();
    }
}