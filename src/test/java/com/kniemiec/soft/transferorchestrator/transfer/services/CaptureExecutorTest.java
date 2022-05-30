package com.kniemiec.soft.transferorchestrator.transfer.services;

import com.kniemiec.soft.transferorchestrator.MockData;
import com.kniemiec.soft.transferorchestrator.payin.DefaultPayIn;
import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payin.model.CaptureStatus;
import com.kniemiec.soft.transferorchestrator.transfer.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class CaptureExecutorTest {


    private CaptureExecutor underTest;


    private DataTransferRepository dataTransferRepository;

    private PayIn payin;

    private TransferProcessor transferProcessor;

    @BeforeEach
    public void setUp(){
        transferProcessor = new TransferProcessor();
        dataTransferRepository = Mockito.mock(DataTransferRepository.class);
        payin = Mockito.mock(DefaultPayIn.class);

        underTest = new CaptureExecutor(dataTransferRepository, payin, transferProcessor);

    }

    @Test
    public void shouldSaveWhenCaptureSucceed(){
        // given
        UUID transferId = UUID.randomUUID();
        UUID lockId = UUID.randomUUID();
        TransferData initialTransferData = MockData.mockTransferData(transferId).withStatus(Status.COMPLIANCE_OK).withLockId(lockId.toString());

        when(payin.capture(lockId.toString())).thenReturn(Mono.just(MockData.mockCaptureResponse(lockId.toString(), CaptureStatus.CAPTURED)));
        when(dataTransferRepository.save(argThat(new TransferDataMatcher(Status.CAPTURED)))).thenReturn(
                Mono.just(MockData.mockTransferData(transferId,lockId)
                        .withLockId(lockId.toString())
                        .withStatus(Status.CAPTURED))
        );

        // when
        transferProcessor.addToQueue(initialTransferData);

        // then
        verify(payin).capture(lockId.toString());
        verify(dataTransferRepository).save(argThat(new TransferDataMatcher(Status.CAPTURED)));
    }

    @Test
    public void shouldNotSaveWhenCaptureFailed(){
        // given
        UUID transferId = UUID.randomUUID();
        UUID lockId = UUID.randomUUID();
        TransferData initialTransferData = MockData.mockTransferData(transferId).withStatus(Status.COMPLIANCE_OK).withLockId(lockId.toString());

        when(payin.capture(lockId.toString())).thenReturn(Mono.just(MockData.mockCaptureResponse(lockId.toString(), CaptureStatus.FAILED)));
        when(dataTransferRepository.save(argThat(new TransferDataMatcher(Status.CAPTURED)))).thenReturn(
                Mono.just(MockData.mockTransferData(transferId,lockId)
                        .withLockId(lockId.toString())
                        .withStatus(Status.CAPTURED))
        );

        // when
        transferProcessor.addToQueue(initialTransferData);

        // then
        verify(payin).capture(lockId.toString());
        verify(dataTransferRepository, times(0)).save(argThat(new TransferDataMatcher(Status.CAPTURED)));
    }
}