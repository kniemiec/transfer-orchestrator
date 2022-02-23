package com.kniemiec.soft.transferorchestrator.payin;

import lombok.Getter;

@Getter
public class PayOutClientException extends RuntimeException{

    private int statusCode;

    public PayOutClientException(String s, int statusCode) {
        super(s);
        this.statusCode = statusCode;
    }
}
