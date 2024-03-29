package com.kniemiec.soft.transferorchestrator.transfer.services;

import com.kniemiec.soft.transferorchestrator.MockData;
import com.kniemiec.soft.transferorchestrator.transfer.ports.ComplianceCheckService;
import com.kniemiec.soft.transferorchestrator.transfer.persistence.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import com.kniemiec.soft.transferorchestrator.transfer.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

class AuthorizationExecutorTest {

    AuthorizationExecutor underTest;

    ComplianceCheckService complianceCheckService;

    DataTransferRepository dataTransferRepository;

    TransferProcessor transferProcessor;

    @BeforeEach
    void setUp() {
        complianceCheckService = Mockito.mock(ComplianceCheckService.class);
        dataTransferRepository = Mockito.mock(DataTransferRepository.class);
        transferProcessor = new TransferProcessor();

        underTest = new AuthorizationExecutor(dataTransferRepository, complianceCheckService, transferProcessor);
    }

    @Test
    void shouldSaveComplianceOKWhenComplianceTrue() {
        // given
        UUID transferId = UUID.randomUUID();
        TransferData initialTransferData = MockData.mockTransferData(transferId).withStatus(Status.LOCKED);
        TransferData savedTransferData = MockData.mockTransferData(transferId).withStatus(Status.COMPLIANCE_OK);

        when(complianceCheckService.check(any(),isA(User.class),isA(User.class))).thenReturn(Mono.just(true));
        when(dataTransferRepository.save(argThat(new TransferDataMatcher(Status.COMPLIANCE_OK)))).thenReturn(Mono.just(savedTransferData));

        // when
        transferProcessor.addToQueue(initialTransferData);

        // then
        verify(dataTransferRepository).save(argThat(new TransferDataMatcher(Status.COMPLIANCE_OK)));
        verify(complianceCheckService).check(any(),isA(User.class), isA(User.class));
    }

    @Test
    void shouldNotSaveComplianceWhenComplianceFalse() {
        // given
        UUID transferId = UUID.randomUUID();
        TransferData initialTransferData = MockData.mockTransferData(transferId).withStatus(Status.LOCKED);

        when(complianceCheckService.check(any(),isA(User.class),isA(User.class))).thenReturn(Mono.just(false));

        // when
        transferProcessor.addToQueue(initialTransferData);

        // then
        verify(dataTransferRepository, times(0)).save(argThat(new TransferDataMatcher(Status.COMPLIANCE_OK)));
        verify(complianceCheckService).check(any(),isA(User.class), isA(User.class));

    }
}