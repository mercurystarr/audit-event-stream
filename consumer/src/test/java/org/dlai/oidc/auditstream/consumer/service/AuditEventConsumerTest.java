package org.dlai.oidc.auditstream.consumer.service;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.dlai.oidc.auditstream.consumer.handler.AuditEventHandler;
import org.dlai.oidc.auditstream.consumer.idempotency.IdempotencyStore;
import org.dlai.oidc.auditstream.proto.audit.v1.AuditEventEnvelope;
import org.dlai.oidc.auditstream.proto.audit.v1.LoginEvent;
import org.dlai.oidc.auditstream.proto.audit.v1.LoginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditEventConsumerTest {

    @Mock
    IdempotencyStore idempotencyStore;
    @Mock
    AuditEventHandler handler;

    AuditEventConsumer consumer;

    @BeforeEach
    public void setUp() {
        consumer = new AuditEventConsumer(idempotencyStore, handler);
    }

    @Test
    public void testConsumerNewRecord() {
        LoginEvent loginEvent = loginEvent().build();
        AuditEventEnvelope envelope = auditEventEnvelope(loginEvent);
        ConsumerRecord<String, byte[]> record = consumerRecord(envelope);
        when(idempotencyStore.markProcessed(envelope.getEventId())).thenReturn(true);

        assertDoesNotThrow(() -> consumer.consume(record));
        verify(handler).handle(envelope);
    }

    @Test
    public void testConsumerDuplicateRecord() {
        LoginEvent loginEvent = loginEvent().build();
        AuditEventEnvelope envelope = auditEventEnvelope(loginEvent);
        ConsumerRecord<String, byte[]> record = consumerRecord(envelope);
        when(idempotencyStore.markProcessed(envelope.getEventId())).thenReturn(false);

        assertDoesNotThrow(() -> consumer.consume(record));
        verify(handler, never()).handle(envelope);
    }

    @Test
    public void testConsumerError() {
        LoginEvent loginEvent = loginEvent().build();
        AuditEventEnvelope envelope = auditEventEnvelope(loginEvent);
        byte[] validBytes = envelope.toByteArray();
        byte[] truncatedBytes = java.util.Arrays.copyOf(validBytes, validBytes.length - 1);
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("test-topic", 0, 0, "test-key", truncatedBytes);

        assertThrows(InvalidProtocolBufferException.class, () -> consumer.consume(record));
        verify(idempotencyStore, never()).markProcessed(anyString());
        verify(handler, never()).handle(envelope);
    }

    @Test
    public void testConsumerDlt() {
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("test-topic.DLT", 0, 0, "test-key", "test-value".getBytes());
        consumer.consumeDlt(record, "test-exception");
        verifyNoInteractions(idempotencyStore, handler);
    }

    private ConsumerRecord<String, byte[]> consumerRecord(AuditEventEnvelope envelope) {
        return new ConsumerRecord<>("test-topic", 0, 0, "test-key", envelope.toByteArray());
    }

    private AuditEventEnvelope auditEventEnvelope(LoginEvent loginEvent) {
        return AuditEventEnvelope.newBuilder()
                .setEventId("event-id")
                .setTenantId("test-tenant")
                .setActorId("user-id")
                .setOccurredAt("occurred-at")
                .setLoginEvent(loginEvent)
                .build();
    }

    private LoginEvent.Builder loginEvent() {
        return LoginEvent.newBuilder()
                .setUserId("user-id")
                .setIpAddress("192.168.1.1")
                .setUserAgent("user-agent")
                .setResult(LoginResult.LOGIN_RESULT_SUCCESS);
    }
}
