package com.kniemiec.soft.transferorchestrator.transfer.services;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DefaultTransferIdGenerator implements TransferIdGenerator {

    @Override
    public UUID generateTransferId(){
        return UUID.randomUUID();
    }
}
