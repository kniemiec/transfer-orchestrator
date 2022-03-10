package com.kniemiec.soft.transferorchestrator.payin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptureResponse {

    String lockId;
    CaptureStatus status;
}
