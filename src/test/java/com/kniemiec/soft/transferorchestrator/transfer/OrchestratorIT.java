package com.kniemiec.soft.transferorchestrator.transfer;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.kniemiec.soft.transferorchestrator.payin.PayIn;
import com.kniemiec.soft.transferorchestrator.payout.PayOut;
import com.kniemiec.soft.transferorchestrator.payout.model.TopUpStatus;
import com.kniemiec.soft.transferorchestrator.transfer.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8090)
@TestPropertySource(
        properties = {
                "payin.url=http://localhost:8090/lock",
                "payout.baseUrl=http://localhost:8090",
                "payout.path.topup=/v1/topup",
                "payout.path.stream=/v1/topups/stream"
        }
)
public class OrchestratorIT {

    @Autowired
    Orchestrator orchestrator;

    @Autowired
    PayIn payIn;

    @Autowired
    PayOut payOut;

    @Autowired
    DataTransferRepository dataTransferRepository;

    @Autowired
    WebTestClient webTestClient;

    @Test
    public void callCreateTransfer() {

        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        stubFor(post("/lock").willReturn(ResponseDefinitionBuilder.responseDefinition().withBody("OK")));
        stubFor(post("/v1/topup").willReturn(ResponseDefinitionBuilder.okForJson(
                MockData.mockTopUpResponseData(transferCreationData.getSenderId(),
                        transferCreationData.getMoney(),
                        TopUpStatus.CREATED)
        )));

        webTestClient.post()
                .uri("/start-transfer")
                .bodyValue(transferCreationData)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(UUID.class)
                .consumeWith(transferIdResult -> {
                    var transferId = transferIdResult.getResponseBody();
                    assertNotNull(transferId);
                });
    }

    @Test
    public void callCreateTransferV2() {

        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        stubFor(post("/lock").willReturn(ResponseDefinitionBuilder.responseDefinition().withBody("OK")));
        stubFor(post("/v1/topup").willReturn(ResponseDefinitionBuilder.okForJson(
                MockData.mockTopUpResponseData(transferCreationData.getSenderId(),
                        transferCreationData.getMoney(),
                        TopUpStatus.CREATED)
        )));

        List<UUID> collectTransferData = new ArrayList<>();

        webTestClient.post()
                .uri("/v2/start-transfer")
                .bodyValue(transferCreationData)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(UUID.class)
                .consumeWith(transferIdResult -> {
                    var transferId = transferIdResult.getResponseBody();
                    assertNotNull(transferId);
                    collectTransferData.add(transferId);
                });

        var data = dataTransferRepository.findById(collectTransferData.get(0).toString())
                .log()
                .block();
        assertEquals(data.getStatus(),Status.TOP_UP_STARTED);

    }

    @Test
    public void callCreateTransferWhenInvalidTopUpUrl() {

        TransferCreationData transferCreationData = MockData.mockTransferCreationData();
        stubFor(post("/lock").willReturn(ResponseDefinitionBuilder.responseDefinition().withBody("OK")));

        stubFor(post("/v1/topup").willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(HttpStatus.BAD_REQUEST.value())));

        webTestClient.post()
                .uri("/start-transfer")
                .bodyValue(transferCreationData)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void callCreateTransferWhenPayInReturns404() {
        stubFor(post("/lock").willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(404)));

        TransferCreationData transferCreationData = MockData.mockInvalidTransferCreationData();

        webTestClient.post()
                .uri("/start-transfer")
                .bodyValue(transferCreationData)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void callCreateTransferWithMissingMandatoryData() {
        stubFor(post("/lock").willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        TransferCreationData transferCreationData = MockData.mockInvalidTransferCreationData();

        webTestClient.post()
                .uri("/start-transfer")
                .bodyValue(transferCreationData)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void callCreateTransferAndReceiveTransferStatus(){
        TransferCreationData transferCreationData = MockData.mockTransferCreationData();

        stubFor(post("/lock").willReturn(ResponseDefinitionBuilder.responseDefinition().withBody("OK")));
        stubFor(post("/v1/topup").willReturn(ResponseDefinitionBuilder.okForJson(
               MockData.mockTopUpResponseData(transferCreationData.getSenderId(),
                        transferCreationData.getMoney(),
                        TopUpStatus.CREATED)
        )));


        List<UUID> receivedTransferId = new ArrayList<>();

        webTestClient.post()
                .uri("/start-transfer")
                .bodyValue(transferCreationData)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(UUID.class)
                .consumeWith( transferIdResult -> {
                    var transferId = transferIdResult.getResponseBody();
                    assertNotNull(transferId);
                    receivedTransferId.add(transferId);
                });

        var transferId = receivedTransferId.get(0);

        webTestClient.get()
                .uri("/transfer-status/"+transferId.toString())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(TransferStatus.class)
                .consumeWith( response -> {
                    var transferStatus = response.getResponseBody();
                    assertEquals(transferStatus.getStatus(),Status.CREATED);
                    assertEquals(transferStatus.getTransferId(), transferId);
                });
    }
}
