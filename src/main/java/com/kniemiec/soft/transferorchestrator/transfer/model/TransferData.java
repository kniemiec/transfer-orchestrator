package com.kniemiec.soft.transferorchestrator.transfer.model;

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

    String lockId = null;

    public TransferData withStatus(Status newStatus){
        return new TransferData(
            this.transferId,
            this.senderId,
            this.recipientId,
            this.senderAddress,
            this.recipientAddress,
            this.getMoney(),
            newStatus,
                this.lockId
        );
    }

    public TransferData withLockId(String lockId){
        return new TransferData(
                this.transferId,
                this.senderId,
                this.recipientId,
                this.senderAddress,
                this.recipientAddress,
                this.getMoney(),
                this.status,
                lockId
        );
    }

    public User getSender(){
        return new User(
                "senderName",
                "senderSurname",
                this.senderId,
                this.senderAddress
        );
    }

    public User getRecipient(){
        return new User(
                "recipientName",
                "recipientSurname",
                this.recipientId,
                this.recipientAddress
        );
    }

}
