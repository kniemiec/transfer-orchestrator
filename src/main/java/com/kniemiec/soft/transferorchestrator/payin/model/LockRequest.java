package com.kniemiec.soft.transferorchestrator.payin.model;

import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LockRequest {

    String transferId;
    Money money;

    public static LockRequest from(Money money, String transferId) {
        return new LockRequest(transferId,money);
    }
}
