package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payin.PayOutClientException;
import com.kniemiec.soft.transferorchestrator.payin.model.LockResponse;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.payout.PayOut;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpResponse;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpStatus;
import com.kniemiec.soft.transferorchestrator.transfer.model.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wiremock.org.eclipse.jetty.http.HttpStatus;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@SpringBootTest
@ActiveProfiles("test")
public class OrchestratorTest {

    @Autowired
    Orchestrator orchestrator;

    @MockBean
    PayIn payIn;

    @MockBean
    PayOut payOut;

    @MockBean
    DataTransferRepository dataTransferRepository;

    @Test
    public void callCreateTransfer(){
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        UUID expectedTransferId = UUID.randomUUID();
        when(payIn.lock(isA(Money.class),isA(String.class))).thenReturn(Mono.just(new LockResponse(expectedTransferId.toString(),transferCreationData.getMoney(), LockStatus.LOCKED)));
        when(payOut.topUp(isA(Money.class), eq(expectedTransferId.toString()),eq(transferCreationData.getSenderId()), eq(transferCreationData.getRecipientId())))
                .thenReturn(Mono.just(new TopUpResponse( transferCreationData.getRecipientId(), transferCreationData.getMoney(),
                        TopUpStatus.CREATED)));
        when(dataTransferRepository.save(any())).thenReturn(Mono.just(transferCreationData.toNewTransferData(expectedTransferId)));
        Mono<UUID> transferIdMono = orchestrator.startTransfer(transferCreationData);
        StepVerifier.create(transferIdMono)
                .expectNext(expectedTransferId)
                .verifyComplete();
        verify(payIn).lock(transferCreationData.getMoney(), transferCreationData.getSenderId());
        verify(payOut).topUp(isA(Money.class), eq(expectedTransferId.toString()), eq(transferCreationData.getSenderId()), eq(transferCreationData.getRecipientId()));
        verify(dataTransferRepository).save(any());
    }

    @Test
    public void callCreateTransferAlternative(){
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        UUID expectedTransferId = UUID.randomUUID();
        when(payIn.lock(isA(Money.class),isA(String.class))).thenReturn(Mono.just(new LockResponse(expectedTransferId.toString(),transferCreationData.getMoney(),LockStatus.LOCKED)));
        when(payOut.topUp(isA(Money.class), eq(expectedTransferId.toString()),eq(transferCreationData.getSenderId()), eq(transferCreationData.getRecipientId())))
                .thenReturn(Mono.just(new TopUpResponse( transferCreationData.getRecipientId(), transferCreationData.getMoney(),
                        TopUpStatus.CREATED)));
        when(dataTransferRepository.save(any())).thenReturn(Mono.just(transferCreationData.toNewTransferData(expectedTransferId)));
        when(dataTransferRepository.findById(expectedTransferId.toString())).thenReturn(Mono.just(transferCreationData.toNewTransferData(expectedTransferId)));
        Mono<UUID> transferIdMono = orchestrator.alternativeStartTransfer(transferCreationData);
        StepVerifier.create(transferIdMono)
                .expectNext(expectedTransferId)
                .verifyComplete();
        verify(payIn).lock(transferCreationData.getMoney(), transferCreationData.getSenderId());
        verify(payOut).topUp(isA(Money.class), eq(expectedTransferId.toString()), eq(transferCreationData.getSenderId()), eq(transferCreationData.getRecipientId()));
        verify(dataTransferRepository, times(3)).save(any());
        verify(dataTransferRepository).findById(expectedTransferId.toString());
    }

    @Test
    public void callCreateTransferWhenPayInFails(){
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        UUID expectedTransferId = UUID.randomUUID();
        when(payIn.lock(isA(Money.class),isA(String.class))).thenReturn(Mono.just(new LockResponse(expectedTransferId.toString(),transferCreationData.getMoney(),LockStatus.REJECTED)));
        when(dataTransferRepository.save(any())).thenReturn(Mono.just(transferCreationData.toNewTransferData(expectedTransferId)));
        Mono<UUID> transferIdMono = orchestrator.startTransfer(transferCreationData);
        StepVerifier.create(transferIdMono)
                .expectError(TransferInitializationFailedException.class);

        verify(payIn).lock(transferCreationData.getMoney(), transferCreationData.getSenderId());
        verify(payOut,times(0) ).topUp(transferCreationData.getMoney(), expectedTransferId.toString(), transferCreationData.getSenderId(), transferCreationData.getRecipientId());
        verify(dataTransferRepository, times(0)).save(any());
    }

    @Test
    public void callCreateTransferWhenPayOutNotFound(){
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        UUID expectedTransferId = UUID.randomUUID();
        when(payIn.lock(isA(Money.class),isA(String.class))).thenReturn(Mono.just(new LockResponse(expectedTransferId.toString(),transferCreationData.getMoney(),LockStatus.LOCKED)));
        when(payOut.topUp(isA(Money.class), eq(expectedTransferId.toString()),eq(transferCreationData.getSenderId()), eq(transferCreationData.getRecipientId())))
                .thenReturn(Mono.error(new PayOutClientException("Exception in Payout Service", HttpStatus.NOT_FOUND_404)));
        when(dataTransferRepository.save(any())).thenReturn(Mono.just(transferCreationData.toNewTransferData(expectedTransferId)));
        Mono<UUID> transferIdMono = orchestrator.startTransfer(transferCreationData);
        StepVerifier.create(transferIdMono)
                .expectError(PayOutClientException.class)
                .verify();
        verify(payIn).lock(transferCreationData.getMoney(), transferCreationData.getSenderId());
        verify(payOut).topUp(isA(Money.class), eq(expectedTransferId.toString()), eq(transferCreationData.getSenderId()), eq(transferCreationData.getRecipientId()));
        verify(dataTransferRepository).save(any());
    }

    @Test
    public void callCreateTransferWhenPayoutFails(){
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        UUID expectedTransferId = UUID.randomUUID();
        when(payIn.lock(isA(Money.class),isA(String.class))).thenReturn(Mono.just(new LockResponse(expectedTransferId.toString(),transferCreationData.getMoney(),LockStatus.LOCKED)));
        when(payOut.topUp(isA(Money.class), eq(expectedTransferId.toString()),eq(transferCreationData.getSenderId()), eq(transferCreationData.getRecipientId())))
                .thenReturn(Mono.just(new TopUpResponse(transferCreationData.getRecipientId(), transferCreationData.getMoney(),
                        TopUpStatus.RETURNED)));
        when(dataTransferRepository.save(any())).thenReturn(Mono.just(transferCreationData.toNewTransferData(expectedTransferId)));
        Mono<UUID> transferIdMono = orchestrator.startTransfer(transferCreationData);
        StepVerifier.create(transferIdMono)
                .expectError(TransferInitializationFailedException.class)
                .verify();
        verify(payIn).lock(transferCreationData.getMoney(), transferCreationData.getSenderId());
        verify(payOut).topUp(isA(Money.class), eq(expectedTransferId.toString()), eq(transferCreationData.getSenderId()), eq(transferCreationData.getRecipientId()));
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


class TransferDataMatcher implements ArgumentMatcher<TransferData>{
    private TransferData left;

    public TransferDataMatcher(TransferData left){
        this.left = left;
    }

    @Override
    public boolean matches(TransferData right){
        return left.getTransferId().equals(right.getTransferId());
    }
}