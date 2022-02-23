package com.kniemiec.soft.transferorchestrator.transfer;

import com.kniemiec.soft.transferorchestrator.payout.TopUpStatusClientRunner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 8091)
public class TopUpStatusClientRunnerTest {


    @SpyBean
    TopUpStatusClientRunner topUpStatusClientRunner;

    @Test
    void topUpClientRunner_starts_on_contextLoads() throws Exception {
        verify(topUpStatusClientRunner,times(1)).run(any());
    }

}
