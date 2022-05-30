package com.kniemiec.soft.transferorchestrator.transfer;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.kniemiec.soft.transferorchestrator.MockData;
import com.kniemiec.soft.transferorchestrator.payin.model.CaptureStatus;
import com.kniemiec.soft.transferorchestrator.payin.model.LockStatus;
import com.kniemiec.soft.transferorchestrator.transfer.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;


@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8090)
@TestPropertySource(
        properties = {
                "payin.url=http://localhost:8090/lock",
                "payout.baseUrl=http://localhost:8090",
                "payout.path.topup=/v1/topup",
                "payout.path.stream=/v1/topups/stream",
                "compliance.check.url=http://localhost:8090/checkCompliance"
        }
)
public class OrchestratorIntegrationTest {

    @Autowired
    DataTransferRepository dataTransferRepository;

    @Autowired
    WebTestClient webTestClient;


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
        String lockId = UUID.randomUUID().toString();
        String notDoneTransferId = UUID.randomUUID().toString();
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();

        stubFor(post("/lock").willReturn(ResponseDefinitionBuilder.okForJson(
                MockData.mockLockResponseData(lockId, LockStatus.LOCKED)
        )));

        stubFor(post("/capture").willReturn(ResponseDefinitionBuilder.okForJson(
                MockData.mockCaptureResponse(lockId,
                        CaptureStatus.CAPTURED)
        ).withFixedDelay(1000)));
        stubFor(put("/checkCompliance").willReturn(ResponseDefinitionBuilder.okForJson(
                MockData.mockComplianceResponseOK(notDoneTransferId)
        ).withFixedDelay(1000)));
//        stubFor(post("/v1/topup").willReturn(ResponseDefinitionBuilder.okForJson(
//                MockData.mockTopUpResponseData(lockId,
//                        transferCreationData.getMoney(),
//                        TopUpStatus.COMPLETED)
//        ).withFixedDelay(2000)));

        List<UUID> receivedTransferId = new ArrayList<>();

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
                    receivedTransferId.add(randomTransferId);
                });

        var transferId = receivedTransferId.get(0);

        Thread.sleep(5000);
        // then
        webTestClient.get()
                .uri("/v1/transfer-status/"+transferId.toString())
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
