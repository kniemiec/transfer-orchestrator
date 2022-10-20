package com.kniemiec.soft.transferorchestrator.transfer;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.kniemiec.soft.transferorchestrator.MockData;
import com.kniemiec.soft.transferorchestrator.payin.model.CaptureStatus;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.transfer.model.*;
import com.kniemiec.soft.transferorchestrator.transfer.services.TransferIdGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8999)
public class OrchestratorIntegrationTest {

    @Autowired
    DataTransferRepository dataTransferRepository;

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    TransferIdGenerator transferIdGenerator;


    @Test
    public void callCreateTransferWhenPayInReturns404() {
        // given
        stubFor(post("/lock").willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(404)));

        TransferCreationData transferCreationData = MockData.mockInvalidTransferCreationData();

        // when
        webTestClient.post()
                .uri("/v1/start-transfer")
                .bodyValue(transferCreationData)
                .exchange()
                .expectStatus()
                // TODO check whether badRequest is good status
                .isBadRequest();
    }

    @Test
    public void callCreateTransferWithMissingMandatoryData() {
        // given
        String lockId = UUID.randomUUID().toString();
        stubFor(post("/lock").willReturn(ResponseDefinitionBuilder.okForJson(
                MockData.mockLockResponseData(lockId, LockStatus.LOCKED)
        )));

        TransferCreationData transferCreationData = MockData.mockInvalidTransferCreationData();

        // when
        webTestClient.post()
                .uri("/v1/start-transfer")
                .bodyValue(transferCreationData)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }


    @Test
    public void fullFlowTest() throws Exception{
        // given
        UUID transferId = UUID.randomUUID();
        String lockId = UUID.randomUUID().toString();
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();

        when(transferIdGenerator.generateTransferId()).thenReturn(transferId);

        stubFor(put("/checkCompliance").willReturn(ResponseDefinitionBuilder.okForJson(
                MockData.mockComplianceResponseOK(transferId.toString())
        )));

        stubFor(post("/lock").willReturn(ResponseDefinitionBuilder.okForJson(
                MockData.mockLockResponseData(lockId, LockStatus.LOCKED)
        )));

        stubFor(post("/capture").willReturn(ResponseDefinitionBuilder.okForJson(
                MockData.mockCaptureResponse(lockId,CaptureStatus.CAPTURED)
        )));

//        stubFor(post("/v1/topup").willReturn(ResponseDefinitionBuilder.okForJson(
//                MockData.mockTopUpResponseData(lockId,
//                        transferCreationData.getMoney(),
//                        TopUpStatus.COMPLETED)
//        ).withFixedDelay(2000)));

        List<UUID> receivedTransferIds = new ArrayList<>();

        // when
        webTestClient.post()
                .uri("/v1/start-transfer")
                .bodyValue(transferCreationData)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(UUID.class)
                .consumeWith( transferIdResult -> {
                    var randomTransferId = transferIdResult.getResponseBody();
                    assertNotNull(randomTransferId);
                    receivedTransferIds.add(randomTransferId);
                });

        var receivedTransferId = receivedTransferIds.get(0);

        System.out.println("Generated transferId: "+transferId.toString());
        System.out.println("Received transferId: "+receivedTransferId.toString());

        Thread.sleep(10000);


        // then
//        verify(postRequestedFor(urlPathEqualTo("/lock")));


        webTestClient.get()
                .uri("/v1/transfer-status/"+receivedTransferId.toString())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(TransferStatus.class)
                .consumeWith( response -> {
                    var transferStatus = response.getResponseBody();
                    assertEquals(Status.CAPTURED, transferStatus.getStatus());
                    assertEquals(transferStatus.getTransferId(), transferId);
                });
    }
}
