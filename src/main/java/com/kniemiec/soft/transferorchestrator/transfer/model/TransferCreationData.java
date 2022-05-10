package com.kniemiec.soft.transferorchestrator.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferCreationData {


    @NotBlank(message = "senderId can not be empty")
    String senderId;

    @NotBlank(message = "recipientId can not be empty")
    String recipientId;

    Address senderAddress;
    Address recipientAddress;

    Money money;


    public TransferData toNewTransferData(UUID transferId) {
        return new TransferData(
                transferId.toString(),
                this.getSenderId(),
                this.getRecipientId(),
                this.getSenderAddress(),
                this.getRecipientAddress(),
                this.getMoney(),
                Status.CREATED,
                null
        );
    }

    public TransferData toLockedTransferData(UUID transferId, String lockId) {
        return new TransferData(
                transferId.toString(),
                this.getSenderId(),
                this.getRecipientId(),
                this.getSenderAddress(),
                this.getRecipientAddress(),
                this.getMoney(),
                Status.LOCKED,
                lockId
        );
    }

    public TransferData toComplianceOkTransferData(UUID expectedTransferId, String lockId) {
        return new TransferData(
                expectedTransferId.toString(),
                this.getSenderId(),
                this.getRecipientId(),
                this.getSenderAddress(),
                this.getRecipientAddress(),
                this.getMoney(),
                Status.COMPLIANCE_OK,
                lockId
        );
    }

    public TransferData toCapturedTransferData(UUID expectedTransferId, String lockId) {
        return new TransferData(
                expectedTransferId.toString(),
                this.getSenderId(),
                this.getRecipientId(),
                this.getSenderAddress(),
                this.getRecipientAddress(),
                this.getMoney(),
                Status.CAPTURED,
                lockId
        );
    }
}
