package com.kniemiec.soft.transferorchestrator.payout.model;

import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopUpResponse {

    String recipientId;

    Money money;

    TopUpStatus status;

    public static TopUpResponse from(TopUpStatusData topUpStatusData){
        return new TopUpResponse(topUpStatusData.getRecipientId(),
                topUpStatusData.getMoney(),
                topUpStatusData.getStatus());
    }
}
