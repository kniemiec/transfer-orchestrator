package com.kniemiec.soft.transferorchestrator.transfer.model;

public enum Status {
    INITIALIZATION_FAILED,
    CREATED,
    LOCKED,
    CAPTURED,
    COMPLIANCHE_CHECK,
    COMPLIANCE_OK,
    TOP_UP_STARTED,
    COMPLETED
}