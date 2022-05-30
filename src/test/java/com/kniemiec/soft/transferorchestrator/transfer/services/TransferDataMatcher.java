package com.kniemiec.soft.transferorchestrator.transfer.services;

import com.kniemiec.soft.transferorchestrator.transfer.model.Status;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import org.mockito.ArgumentMatcher;

public class TransferDataMatcher implements ArgumentMatcher<TransferData> {

    private Status status;

    public TransferDataMatcher(Status status){
        this.status = status;
    }

    @Override
    public boolean matches(TransferData argument) {
        return argument.getStatus().equals(this.status);
    }
}