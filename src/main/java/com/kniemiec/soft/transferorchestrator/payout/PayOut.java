package com.kniemiec.soft.transferorchestrator.payout;

import com.kniemiec.soft.transferorchestrator.payout.model.TopUpResponse;
import com.kniemiec.soft.transferorchestrator.transfer.model.TransferData;
import reactor.core.publisher.Mono;

public interface PayOut {
    Mono<TopUpResponse> topUp(TransferData transferData);
}
