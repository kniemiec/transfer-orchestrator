package com.kniemiec.soft.transferorchestrator.transfer.services;

import com.kniemiec.soft.transferorchestrator.payout.DefaultPayOut;
import com.kniemiec.soft.transferorchestrator.transfer.ports.PayOut;
import com.kniemiec.soft.transferorchestrator.transfer.persistence.DataTransferRepository;
import com.kniemiec.soft.transferorchestrator.transfer.TransferProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TopUpExecutorTest {

    TopUpExecutor underTest;

    private PayOut payOut;

    private TransferProcessor transferProcessor;

    private DataTransferRepository dataTransferRepository;


    @BeforeEach
    public void setUp(){
        transferProcessor = new TransferProcessor();
        dataTransferRepository = Mockito.mock(DataTransferRepository.class);
        payOut = Mockito.mock(DefaultPayOut.class);
        underTest = new TopUpExecutor(transferProcessor, payOut, dataTransferRepository);
    }


    @Test
    public void shouldSaveWhenTopUpStarted(){

    }

    @Test
    public void shouldNotSaveWhenTopUpFailed(){

    }

}
