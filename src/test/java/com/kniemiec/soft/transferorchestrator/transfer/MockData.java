package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.payin.model.LockResponse;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpResponse;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpStatus;
import com.kniemiec.soft.transferorchestrator.transfer.model.*;

import java.math.BigDecimal;
import java.util.UUID;

public class MockData {

    public static TransferData mockTransferData(UUID transferId){
       return new TransferData(
                transferId.toString(),
                "senderId",
                "recipientId",
                new Address("Kanadyjska", "Zielonki", "32-087", "Poland"),
                new Address("Kołłątajowskiej", "Krakóœ", "31-234", "Poland"),
                new Money("CHF", BigDecimal.valueOf(1000)),
               Status.CREATED
        );
    }

    public static TransferData mockTransferData(){
        UUID transferId = UUID.randomUUID();
        return mockTransferData(transferId);
    }

    public static TransferCreationData mockTransferCreationData(){
        return new TransferCreationData(
                "senderId",
                "recipientId",
                new Address("Kanadyjska", "Zielonki", "32-087", "Poland"),
                new Address("Kołłątajowskiej", "Krakóœ", "31-234", "Poland"),
                new Money("CHF", BigDecimal.valueOf(1000))
        );
    }

    public static TransferStatus mockTransferStatusData(UUID transferId) {
        return new TransferStatus(
                transferId,
                Status.CREATED
        );
    }

    public static TransferCreationData mockInvalidTransferCreationData() {
        return new TransferCreationData(
                "senderId",
                "",
                new Address("Kanadyjska", "Zielonki", "32-087", "Poland"),
                new Address("Kołłątajowskiej", "Krakóœ", "31-234", "Poland"),
                // TODO - why this is 'valid'
                new Money("", new BigDecimal(0.0))
        );
    }

    public static LockResponse mockLockResponseData(String senderId, Money money, LockStatus lockStatus) {
        return new LockResponse(senderId, money, lockStatus);
    }

    public static TopUpResponse mockTopUpResponseData(String recipientId, Money money, TopUpStatus topUpStatus) {
        return new TopUpResponse(
                recipientId,
                money,
                topUpStatus
        );
    }
}
