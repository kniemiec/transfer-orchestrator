package com.kniemiec.soft.transferorchestrator.compliance;

import com.kniemiec.soft.transferorchestrator.MockData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.verify;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//@AutoConfigureWebTestClient
//@AutoConfigureWireMock(port = 8092)
class DefaultComplianceCheckClientTest {


    @Autowired
    private DefaultComplianceCheckClient complianceCheckClient;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp(){
    }


    @Test
    @Disabled
    void check() {
        // given

        // when
        complianceCheckClient.check("transferId", MockData.mockSenderUserData(), MockData.mockRecipientUserData() );

        // then
        verify(webTestClient.put());

    }
}