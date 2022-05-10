package com.kniemiec.soft.transferorchestrator.transfer;

public class TransferComplianceCheckException extends RuntimeException{
    private String message;

    public TransferComplianceCheckException(String message){
        super(message);
        this.message = message;
    }
}
