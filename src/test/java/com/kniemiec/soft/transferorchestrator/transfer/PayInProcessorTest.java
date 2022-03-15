package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.payin.model.LockResponse;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PayInProcessorTest {

    PayInProcessor payInProcessor;

    int counter;

    @BeforeEach
    void setUp(){
        payInProcessor = new PayInProcessor();
        counter = 0;
    }

    @Test
    void tryEmitNext() {
        payInProcessor.onLockResponseReceived(getMockSubscriber());

        payInProcessor.addToQueue(new LockResponse("1", new Money("PLN", BigDecimal.valueOf(100)), LockStatus.LOCKED));
        payInProcessor.addToQueue(new LockResponse("2", new Money("PLN", BigDecimal.valueOf(100)), LockStatus.LOCKED));
        payInProcessor.addToQueue(new LockResponse("3", new Money("PLN", BigDecimal.valueOf(100)), LockStatus.LOCKED));

        assertEquals(3, counter);

    }


    Subscriber<LockResponse> getMockSubscriber(){
        return new Subscriber<LockResponse>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(LockResponse lockResponse) {
                counter++;
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        };
    }
}