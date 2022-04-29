package com.kniemiec.soft.transferorchestrator.compliance.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceRequest {

    String transferId;

    UserData senderData;

    UserData recipientData;
}
