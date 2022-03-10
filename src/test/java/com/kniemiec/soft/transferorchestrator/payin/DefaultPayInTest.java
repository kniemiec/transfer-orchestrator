package com.kniemiec.soft.transferorchestrator.payin;

import com.kniemiec.soft.transferorchestrator.transfer.model.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultPayInTest {


    PayIn payIn;

    @Mock
    WebClient webClient;


    @BeforeEach
    void setUp(){
        webClient = Mockito.mock(WebClient.class);
        payIn = new DefaultPayIn(webClient);
    }

    @Test
    @Disabled
    void lock() {
        // given

        // when
        payIn.lock(new Money("PLN", BigDecimal.valueOf(100)), "senderId");

        // then
        verify(webClient.post());
    }

    @Test
    void testLock() {
    }
}