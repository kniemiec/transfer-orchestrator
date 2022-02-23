package com.kniemiec.soft.transferorchestrator.payout.model;

import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopUpData {

    UUID transferId;

    String senderId;

    String recipientId;

    Money money;

    public static TopUpData from(UUID transferId, String senderId, String recipient, Money money){
        return new TopUpData(
                transferId,
                senderId,
                recipient,
                money
        );
    }

}
