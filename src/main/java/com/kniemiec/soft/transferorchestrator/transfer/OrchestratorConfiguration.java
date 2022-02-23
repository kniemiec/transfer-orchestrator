package com.kniemiec.soft.transferorchestrator.transfer;


import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payout.PayOut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrchestratorConfiguration {

//    @Bean TransferController getTransferController(Orchestrator orchestrator){
//        return new TransferController(orchestrator);
//    }

    @Bean
    public Orchestrator getOrchestrator(PayIn payin, PayOut payout, DataTransferRepository dataTransferRepository){
        return new Orchestrator(payin,payout, dataTransferRepository);
    }
}
