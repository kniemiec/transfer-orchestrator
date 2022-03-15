package com.kniemiec.soft.transferorchestrator.transfer.model;

import com.kniemiec.soft.transferorchestrator.payout.model.TopUpStatusData;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TransferStatus {

    UUID transferId;

    Status status;

    public static TransferStatus from(TransferData transferData){
        return new TransferStatus(
                UUID.fromString(transferData.getTransferId()),
                transferData.getStatus()
        );
    }
}
