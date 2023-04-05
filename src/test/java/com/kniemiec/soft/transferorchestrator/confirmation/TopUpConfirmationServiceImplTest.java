package com.kniemiec.soft.transferorchestrator.confirmation;


import com.kniemiec.soft.transferorchestrator.topup.TopUpRequest;
import com.kniemiec.soft.transferorchestrator.topup.TopUpResponse;
import com.kniemiec.soft.transferorchestrator.transfer.persistence.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class TopUpConfirmationServiceImplTest {

    @Mock
    private TransferProcessor transferProcessor;

    @Mock
    private DataTransferRepository dataTransferRepository;

    @InjectMocks
    private TopUpConfirmationServiceImpl topUpConfirmationService;

    @Test
    public void topUpCompleted() {
        TopUpRequest request = TopUpRequest.newBuilder().setTransferId("1").build();
        TopUpResponse response = TopUpResponse.newBuilder().setTransferId("1").build();
        when(dataTransferRepository.findById(anyString())).thenReturn(Mono.just(new com.kniemiec.soft.transferorchestrator.transfer.model.TransferData()));
        when(dataTransferRepository.save(any())).thenReturn(Mono.just(new com.kniemiec.soft.transferorchestrator.transfer.model.TransferData()));
        when(transferProcessor.addToQueue(any())).thenReturn(new com.kniemiec.soft.transferorchestrator.transfer.model.TransferData());
        topUpConfirmationService.topUpCompleted(request, new StreamObserver<TopUpResponse>() {
            @Override
            public void onNext(TopUpResponse value) {
                assert value.getTransferId().equals("1");
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        verify(dataTransferRepository, times(1)).findById(anyString());
        verify(dataTransferRepository, times(1)).save(any());
        verify(transferProcessor, times(1)).addToQueue(any());
    }
}
