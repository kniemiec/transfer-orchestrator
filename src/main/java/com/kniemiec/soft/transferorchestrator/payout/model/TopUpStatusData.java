package com.kniemiec.soft.transferorchestrator.payout.model;

import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class TopUpStatusData {

    @Id
    String id;

    String senderId;
    String recipientId;

    Money money;

    TopUpStatus status;
}
