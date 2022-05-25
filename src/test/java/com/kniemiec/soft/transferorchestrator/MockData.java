package com.kniemiec.soft.transferorchestrator;

import com.kniemiec.soft.transferorchestrator.compliance.model.ComplianceProblem;
import com.kniemiec.soft.transferorchestrator.compliance.model.ComplianceResponse;
import com.kniemiec.soft.transferorchestrator.compliance.model.ComplianceStatus;
import com.kniemiec.soft.transferorchestrator.payin.model.CaptureResponse;
import com.kniemiec.soft.transferorchestrator.payin.model.CaptureStatus;
import com.kniemiec.soft.transferorchestrator.payin.model.LockResponse;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpResponse;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpStatus;
import com.kniemiec.soft.transferorchestrator.transfer.model.*;

import java.math.BigDecimal;
import java.util.Collections;
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
               Status.CREATED,
               null
        );
    }

    public static TransferData mockTransferData(UUID transferId, UUID captureId){
        return new TransferData(
                transferId.toString(),
                "senderId",
                "recipientId",
                new Address("Kanadyjska", "Zielonki", "32-087", "Poland"),
                new Address("Kołłątajowskiej", "Krakóœ", "31-234", "Poland"),
                new Money("CHF", BigDecimal.valueOf(1000)),
                Status.CREATED,
                captureId.toString()
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
                // TODO - why this is 'valid' - validatio should fail here
                new Money("", BigDecimal.valueOf(0))
        );
    }

    public static LockResponse mockLockResponseData(String lockId, LockStatus lockStatus) {
        return new LockResponse(lockId, lockStatus);
    }

    public static CaptureResponse mockCaptureResponse(String lockId, CaptureStatus status) {
        return new CaptureResponse(
                lockId,
                status
        );
    }


    public static TopUpResponse mockTopUpResponseData(String transferId, Money money, TopUpStatus topUpStatus) {
        return new TopUpResponse(
                transferId,
                money,
                topUpStatus
        );
    }

    public static User mockSenderUserData() {
        return new User("Jan", "Kowalski", "1", new Address(
                "Ulica",
                "Miasto",
                "postalCode",
                "Poland"
        ));
    }

    public static User mockRecipientUserData() {
        return new User("Jan", "Kowalski", "2", new Address(
                "Ulica",
                "Miasto",
                "postalCode",
                "Poland"
        ));
    }

    public static ComplianceResponse mockComplianceResponseOK(String transferId) {
        return new ComplianceResponse(
                transferId,
                Collections.emptyList(),
                Collections.emptyList(),
                ComplianceStatus.OK
        );
    }

    public static ComplianceResponse mockComplianceResponseAlert(String transferId) {
        return new ComplianceResponse(
                transferId,
                Collections.singletonList(new ComplianceProblem("firstSenderProblem","User on sanction list")),
                Collections.singletonList(new ComplianceProblem("firstReceiverProblem", "User on sanction list")),
                ComplianceStatus.ALERT
        );
    }
}
