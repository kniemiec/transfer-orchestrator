package com.kniemiec.soft.transferorchestrator.compliance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplianceResponse {

    List<ComplianceProblem> senderStatus;

    List<ComplianceProblem> recipientStatus;

    ComplianceStatus status;

}
