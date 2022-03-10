package com.kniemiec.soft.transferorchestrator.transfer.model;

import com.kniemiec.soft.transferorchestrator.payout.model.TopUpStatusData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferData {

    @Id
    String transferId;

    String senderId;

    String recipientId;

    Address senderAddress;
    Address recipientAddress;

    Money money;

    Status status;

    String captureId = null;

    public TransferData withStatus(Status newStatus){
        return new TransferData(
            this.transferId,
            this.senderId,
            this.recipientId,
            this.senderAddress,
            this.recipientAddress,
            this.getMoney(),
            newStatus,
                null
        );
    }

}
