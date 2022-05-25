package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.MockData;
import com.kniemiec.soft.transferorchestrator.compliance.ComplianceCheckService;
import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payin.PayOutClientException;
import com.kniemiec.soft.transferorchestrator.payin.model.CaptureResponse;
import com.kniemiec.soft.transferorchestrator.payin.model.CaptureStatus;
import com.kniemiec.soft.transferorchestrator.payin.model.LockResponse;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.transfer.model.*;
import com.kniemiec.soft.transferorchestrator.transfer.services.StartTransferAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wiremock.org.eclipse.jetty.http.HttpStatus;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class OrchestratorTest {

    Orchestrator orchestrator;

    PayIn payIn;

    ComplianceCheckService complianceCheckService;

    DataTransferRepository dataTransferRepository;

    StartTransferAction startTransferAction;

    @BeforeEach
    void setUp(){
        payIn = Mockito.mock(PayIn.class);
        complianceCheckService = Mockito.mock(ComplianceCheckService.class);
        dataTransferRepository = Mockito.mock(DataTransferRepository.class);
        startTransferAction = Mockito.mock(StartTransferAction.class);

        orchestrator = new Orchestrator(
                payIn,
                complianceCheckService,
                new TransferProcessor(),
                dataTransferRepository,
                startTransferAction
        );
    }

    @Test
    public void callCreateTransfer(){
        // given
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();


        UUID expectedTransferId = UUID.randomUUID();
        UUID lockId = UUID.randomUUID();

        when(payIn.lock(isA(Money.class),isA(String.class)))
                .thenReturn(Mono.just(new LockResponse(lockId.toString(),LockStatus.LOCKED)));
        when(complianceCheckService.check(eq(expectedTransferId.toString()),isA(User.class), isA(User.class)))
                .thenReturn(Mono.just(true));
        when(payIn.capture(eq(lockId.toString())))
                .thenReturn(Mono.just(new CaptureResponse(lockId.toString(), CaptureStatus.CAPTURED)));
        when(dataTransferRepository.save(any()))
                .thenReturn(
                        Mono.just(transferCreationData.toNewTransferData(expectedTransferId)),
                        Mono.just(transferCreationData.toLockedTransferData(expectedTransferId, lockId.toString())),
                        Mono.just(transferCreationData.toComplianceOkTransferData(expectedTransferId, lockId.toString())),
                        Mono.just(transferCreationData.toCapturedTransferData(expectedTransferId,lockId.toString()))
                );

        // when
        Mono<UUID> transferIdMono = orchestrator.startTransfer(transferCreationData);

        // then
        StepVerifier.create(transferIdMono)
                .expectNext(expectedTransferId)
                .verifyComplete();
        verify(payIn).lock(transferCreationData.getMoney(), transferCreationData.getSenderId());
        verify(complianceCheckService).check(eq(expectedTransferId.toString()), isA(User.class), isA(User.class));
        verify(payIn).capture(eq(lockId.toString()));
        verify(dataTransferRepository, times(4)).save(any());
    }

    @Test
    public void callCreateTransferWhenPayInFails(){
        // given
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        UUID expectedTransferId = UUID.randomUUID();
        UUID lockId = UUID.randomUUID();
        when(payIn.lock(eq(transferCreationData.getMoney()),eq(transferCreationData.getSenderId())))
                .thenReturn(Mono.just(new LockResponse(lockId.toString(),LockStatus.REJECTED)));
        when(dataTransferRepository.save(any()))
                .thenReturn(Mono.just(transferCreationData.toNewTransferData(expectedTransferId)));

        // when
        Mono<UUID> transferIdMono = orchestrator.startTransfer(transferCreationData);

        // then
        StepVerifier.create(transferIdMono)
                .expectError(TransferInitializationFailedException.class)
                        .verify();

        verify(payIn).lock(eq(transferCreationData.getMoney()), eq(transferCreationData.getSenderId()));
        verify(payIn,times(0) ).capture(isA(String.class));
        verify(dataTransferRepository, times(1)).save(any());
    }

    @Test
    public void callCreateTransferWhenComplianceAlert(){
        // given
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        UUID expectedTransferId = UUID.randomUUID();
        UUID lockId = UUID.randomUUID();
        when(payIn.lock(isA(Money.class),isA(String.class)))
                .thenReturn(Mono.just(new LockResponse(lockId.toString(),LockStatus.LOCKED)));
        when(dataTransferRepository.save(any()))
                .thenReturn(
                        Mono.just(transferCreationData.toNewTransferData(expectedTransferId)),
                        Mono.just(transferCreationData.toLockedTransferData(expectedTransferId, lockId.toString()))
                );
        when(complianceCheckService.check(eq(expectedTransferId.toString()),isA(User.class), isA(User.class)))
                .thenReturn(Mono.just(false));


        // when
        Mono<UUID> transferIdMono = orchestrator.startTransfer(transferCreationData);

        // then
        StepVerifier.create(transferIdMono)
                .expectNext(expectedTransferId)
                .verifyComplete();

        verify(payIn).lock(eq(transferCreationData.getMoney()), eq(transferCreationData.getSenderId()));
        verify(complianceCheckService).check(eq(expectedTransferId.toString()), isA(User.class), isA(User.class));
        verify(payIn,times(0) ).capture(isA(String.class));
        verify(dataTransferRepository, times(2)).save(any());
    }

    @Test
    public void callCreateTransferWhenComplianceFails(){
        // given
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        UUID expectedTransferId = UUID.randomUUID();
        UUID lockId = UUID.randomUUID();
        when(payIn.lock(isA(Money.class),isA(String.class)))
                .thenReturn(Mono.just(new LockResponse(lockId.toString(),LockStatus.LOCKED)));
        when(dataTransferRepository.save(any()))
                .thenReturn(
                        Mono.just(transferCreationData.toNewTransferData(expectedTransferId)),
                        Mono.just(transferCreationData.toLockedTransferData(expectedTransferId, lockId.toString()))
                );
        when(complianceCheckService.check(eq(expectedTransferId.toString()),isA(User.class), isA(User.class)))
                .thenReturn(Mono.empty());


        // when
        Mono<UUID> transferIdMono = orchestrator.startTransfer(transferCreationData);

        // then
        StepVerifier.create(transferIdMono)
                .expectNext(expectedTransferId)
                .verifyComplete();

        verify(payIn).lock(eq(transferCreationData.getMoney()), eq(transferCreationData.getSenderId()));
        verify(payIn,times(0) ).capture(isA(String.class));
        verify(dataTransferRepository, times(2)).save(any());
    }


    @Test
    @Disabled
    public void callCreateTransferWhenPayOutNotFound(){
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        UUID expectedTransferId = UUID.randomUUID();
        when(payIn.lock(isA(Money.class),isA(String.class))).thenReturn(Mono.just(new LockResponse(expectedTransferId.toString(),LockStatus.LOCKED)));
        when(complianceCheckService.check(eq(expectedTransferId.toString()), isA(User.class),isA(User.class)))
                .thenReturn(Mono.error(new PayOutClientException("Exception in Payout Service", HttpStatus.NOT_FOUND_404)));
        when(dataTransferRepository.save(any())).thenReturn(Mono.just(transferCreationData.toNewTransferData(expectedTransferId)));
        Mono<UUID> transferIdMono = orchestrator.startTransfer(transferCreationData);
        StepVerifier.create(transferIdMono)
                .expectError(PayOutClientException.class)
                .verify();
        verify(payIn).lock(transferCreationData.getMoney(), transferCreationData.getSenderId());
        verify(complianceCheckService).check(eq(expectedTransferId.toString()), isA(User.class),isA(User.class));
        verify(dataTransferRepository).save(any());
    }

    @Test
    @Disabled
    public void callCreateTransferWhenPayoutFails(){
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        UUID expectedTransferId = UUID.randomUUID();
        when(payIn.lock(isA(Money.class),isA(String.class))).thenReturn(Mono.just(new LockResponse(expectedTransferId.toString(),LockStatus.LOCKED)));
        when(complianceCheckService.check(eq(expectedTransferId.toString()), isA(User.class),isA(User.class)))
                .thenReturn(Mono.just(false));
        when(dataTransferRepository.save(any())).thenReturn(Mono.just(transferCreationData.toNewTransferData(expectedTransferId)));
        Mono<UUID> transferIdMono = orchestrator.startTransfer(transferCreationData);
        StepVerifier.create(transferIdMono)
                .expectError(TransferInitializationFailedException.class)
                .verify();
        verify(payIn).lock(transferCreationData.getMoney(), transferCreationData.getSenderId());
        verify(complianceCheckService).check(eq(expectedTransferId.toString()), isA(User.class),isA(User.class));
        verify(dataTransferRepository).save(any());
    }

    @Test
    public void callGetTransferStatus(){
        UUID transferId = UUID.randomUUID();
        when(dataTransferRepository.findById(transferId.toString())).thenReturn(Mono.just(MockData.mockTransferData(transferId)));
        Mono<TransferStatus> transferStatusMono = orchestrator.getTransferStatus(transferId);

        StepVerifier.create(transferStatusMono)
                .expectNextMatches( transferStatus -> transferStatus.getStatus() == Status.CREATED && transferStatus.getTransferId().equals(transferId))
                .verifyComplete();
        verify(dataTransferRepository).findById(transferId.toString());

    }
}