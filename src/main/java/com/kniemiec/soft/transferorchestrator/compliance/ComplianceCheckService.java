package com.kniemiec.soft.transferorchestrator.compliance;

import com.kniemiec.soft.transferorchestrator.compliance.model.ComplianceResponse;
import com.kniemiec.soft.transferorchestrator.transfer.model.User;
import reactor.core.publisher.Mono;

public interface ComplianceCheckService {

    Mono<Boolean> check(String transferId, User sender, User recipient);

}
