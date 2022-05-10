package com.kniemiec.soft.transferorchestrator.transfer.model;

public enum Status {
    INITIALIZATION_FAILED,
    CREATED,
    LOCKED,
    CAPTURED,
    COMPLIANCE_OK,
    COMPLIANCE_ALERT,
    TOP_UP_STARTED,
    COMPLETED
}