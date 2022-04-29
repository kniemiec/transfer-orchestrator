package com.kniemiec.soft.transferorchestrator.compliance;

import com.kniemiec.soft.transferorchestrator.MockData;

import com.kniemiec.soft.transferorchestrator.transfer.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.Mockito.verify;

class DefaultComplianceCheckClientTest {


    private DefaultComplianceCheckClient complianceCheckClient;

    @Mock
    WebClient webClient;

    @BeforeEach
    void setUp(){
        webClient = Mockito.mock(WebClient.class);
        complianceCheckClient = new DefaultComplianceCheckClient(webClient);
    }


    @Test
    void check() {
        // given

        // when
        complianceCheckClient.check("transferId", MockData.mockSenderUserData(), MockData.mockRecipientUserData() );

        // then
        verify(webClient.put());

    }
}