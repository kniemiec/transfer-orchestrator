package com.kniemiec.soft.transferorchestrator.transfer.errors;

import com.kniemiec.soft.transferorchestrator.payin.PayOutClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.constraints.NotNull;

@Slf4j
@ControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(PayOutClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleLockClientException(PayOutClientException exception){
        log.error("Exception Caught when calling payout service");
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
    }

}
