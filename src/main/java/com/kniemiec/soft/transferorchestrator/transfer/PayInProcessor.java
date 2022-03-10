package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payin.model.LockResponse;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
@Service
public class PayInProcessor {

    private Sinks.Many<LockResponse> lockResponses;

    public void onLockResponseReceived(Subscriber<LockResponse> subscriber){
        log.info("onLockResponseReceived called");
        lockResponses.asFlux().subscribe(subscriber);
    }

    public PayInProcessor(){
        this.lockResponses = Sinks.many().replay().latest();
    }

    public void tryEmitNext(LockResponse lockResponse){
        lockResponses.tryEmitNext(lockResponse);
    }

    public Flux<LockResponse> exposeFlux(){
        return lockResponses.asFlux();
    }
}
