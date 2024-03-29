package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.payin.model.CaptureResponse;
import com.kniemiec.soft.transferorchestrator.payin.model.LockResponse;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.locks.Lock;

@Slf4j
@Service
public class PayInProcessor {

    private Sinks.Many<LockResponse> lockResponses;

    private Sinks.Many<CaptureResponse> captureResponses;


    public void onLockResponseReceived(Subscriber<LockResponse> subscriber){
        log.info("onLockResponseReceived called");
        lockResponses.asFlux().subscribe(subscriber);
    }

    public PayInProcessor(){
        this.captureResponses = Sinks.many().replay().latest();
        this.lockResponses = Sinks.many().replay().latest();
    }

    public LockResponse addToQueue(LockResponse lockResponse){
        lockResponses.tryEmitNext(lockResponse);
        return lockResponse;
    }


    public CaptureResponse addToQueue(CaptureResponse captureResponse){
        captureResponses.tryEmitNext(captureResponse);
        return captureResponse;
    }

    public Flux<LockResponse> exposeLockQueue(){
        return lockResponses.asFlux();
    }

    public Flux<LockResponse> exposeCaptureQueue(){
        return lockResponses.asFlux();
    }
}
