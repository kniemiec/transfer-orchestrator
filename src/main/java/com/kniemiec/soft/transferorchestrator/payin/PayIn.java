package com.kniemiec.soft.transferorchestrator.payin;

import com.kniemiec.soft.transferorchestrator.payin.model.CaptureResponse;
import com.kniemiec.soft.transferorchestrator.payin.model.LockResponse;
import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import reactor.core.publisher.Mono;

public interface PayIn {

    Mono<LockResponse> lock(Money money, String senderId);


    Mono<CaptureResponse> capture(String lockId);
}
