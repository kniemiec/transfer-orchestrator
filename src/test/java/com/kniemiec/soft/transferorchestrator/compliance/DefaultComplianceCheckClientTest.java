package com.kniemiec.soft.transferorchestrator.compliance;

import com.kniemiec.soft.compliance.ComplianceCheckGrpc;
import com.kniemiec.soft.compliance.ComplianceCheckResponse;
import com.kniemiec.soft.transferorchestrator.MockData;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.grpcmock.springboot.AutoConfigureGrpcMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.grpcmock.GrpcMock.*;


@DirtiesContext
@SpringBootTest
@AutoConfigureGrpcMock
@ActiveProfiles("test")
class DefaultComplianceCheckClientTest {

    @Value("${grpcmock.server.port}")
    private int grpcMockPort;

//    private ManagedChannel managedChannel;

    @Autowired
    private DefaultComplianceCheckClient complianceCheckClient;

    @BeforeEach
    void setUp(){
    }
//
//    @AfterEach
//    void shutdownChannel() {
//        Optional.ofNullable(managedChannel).ifPresent(ManagedChannel::shutdownNow);
//    }


    @Test
    @Disabled
    void check() {
        // given
        stubFor(unaryMethod(ComplianceCheckGrpc.getCheckComplianceMethod())
                .willReturn(ComplianceCheckResponse.newBuilder().build()));


        // when
        complianceCheckClient.check("transferId", MockData.mockSenderUserData(), MockData.mockRecipientUserData() );

        // then
        verifyThat(calledMethod(ComplianceCheckGrpc.getCheckComplianceMethod()));

    }
}