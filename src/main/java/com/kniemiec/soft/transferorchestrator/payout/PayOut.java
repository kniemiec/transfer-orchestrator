package com.kniemiec.soft.transferorchestrator.payout;

import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpResponse;
import reactor.core.publisher.Mono;

public interface PayOut {
    Mono<TopUpResponse> topUp(Money money, String transferId, String senderId, String recipientId);
}
