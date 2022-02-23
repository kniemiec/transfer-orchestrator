package com.kniemiec.soft.transferorchestrator.transfer;

public class TransferInitializationFailedException extends RuntimeException {

    private String message;

    public TransferInitializationFailedException(String message){
        super(message);
        this.message = message;
    }
}
