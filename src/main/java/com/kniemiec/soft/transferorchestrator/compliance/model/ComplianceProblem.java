package com.kniemiec.soft.transferorchestrator.compliance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplianceProblem {
    String problemId;
    String problemDescription;
}
