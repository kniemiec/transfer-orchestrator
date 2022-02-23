package com.kniemiec.soft.transferorchestrator.payin.model;

import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LockRequest {

    String senderId;

    Money money;

    public static LockRequest from(Money money, String senderId) {
        return new LockRequest(senderId,money);
    }
}
