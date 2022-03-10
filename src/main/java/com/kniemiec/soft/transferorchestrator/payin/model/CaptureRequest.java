package com.kniemiec.soft.transferorchestrator.payin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptureRequest {
    String lockId;

    public static CaptureRequest from(String lockId) {
        return new CaptureRequest(lockId);
    }
}
