package org.dlai.oidc.auditstream.producer.service;

import com.google.protobuf.InvalidProtocolBufferException;
import org.dlai.oidc.auditstream.proto.audit.v1.AuditEventEnvelope;
import org.dlai.oidc.auditstream.proto.audit.v1.LoginEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventPublisherTest {

    @Mock
    KafkaTemplate<String, byte[]> kafkaTemplate;
    AuditEventPublisher publisher;


    private AuditEventEnvelope.Builder getEnvelope() {
        return AuditEventEnvelope.newBuilder()
                .setTenantId("test-tenant")
                .setActorId("test-actor")
                .setLoginEvent(LoginEvent.newBuilder()
                        .setUserId("test-user")
                        .setIpAddress("127.0.0.1")
                        .build());

    }

    @BeforeEach
    public void setUp() {
        publisher = new AuditEventPublisher(kafkaTemplate, "test-topic");
    }

    @Test
    public void testPublishAndVerifyEvent() {
        AuditEventEnvelope.Builder envelope = getEnvelope();

        publisher.publish(envelope);

        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(kafkaTemplate).send(eq("test-topic"), eq("test-tenant"), payloadCaptor.capture());

        try {
            AuditEventEnvelope event = AuditEventEnvelope.parseFrom(payloadCaptor.getValue());
            LoginEvent loginEvent = event.getLoginEvent();

            assertAll(
                    () -> assertFalse(event.getEventId().isBlank()),
                    () -> assertFalse(event.getOccurredAt().isBlank()),
                    () -> assertEquals("test-tenant", event.getTenantId()),
                    () -> assertEquals("test-actor", event.getActorId()),
                    () -> assertEquals("test-user", loginEvent.getUserId()),
                    () -> assertEquals("127.0.0.1", loginEvent.getIpAddress())
            );

        } catch (InvalidProtocolBufferException e) {
            fail("Unable to parse payload: " + e.getMessage());
        }
    }
}
