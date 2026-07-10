package org.dlai.oidc.auditstream.producer.controller;

import org.dlai.oidc.auditstream.producer.service.AuditEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditEventControllerTest {

    @Mock
    AuditEventPublisher publisher;
    AuditEventController controller;

    @BeforeEach
    public void setUp() {
        controller = new AuditEventController(publisher);
    }

    @Test
    public void testPublishLoginEventSuccess() {
        stubPublishSuccess();
        ResponseEntity<Map<String, String>> responseEntity = controller.publishLoginEvent("test-tenant", "test-user", "127.0.0.1", "Mozilla/5.0", "SUCCESS").join();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Login event published", responseEntity.getBody().get("status"));
    }

    @Test
    public void testPublishLoginEventError() {
        stubPublishFailure();
        ResponseEntity<Map<String, String>> responseEntity = controller.publishLoginEvent("test-tenant", "test-user", "127.0.0.1", "Mozilla/5.0", "SUCCESS").join();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Kafka unavailable", responseEntity.getBody().get("error"));
    }

    @Test
    public void testPublishPermissionChangedEventSuccess() {
        stubPublishSuccess();
        ResponseEntity<Map<String, String>> responseEntity = controller.publishPermissionChangedEvent("test-tenant", "test-user", "target-user", "permission", "GRANTED", "reason").join();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Permission changed event published", responseEntity.getBody().get("status"));
    }

    @Test
    public void testPublishPermissionChangedEventError() {
        stubPublishFailure();
        ResponseEntity<Map<String, String>> responseEntity = controller.publishPermissionChangedEvent("test-tenant", "test-user", "target-user", "permission", "GRANTED", "reason").join();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Kafka unavailable", responseEntity.getBody().get("error"));
    }

    @Test
    public void testPublishAccountUpdatedEventSuccess() {
        stubPublishSuccess();
        ResponseEntity<Map<String, String>> responseEntity = controller.publishAccountUpdatedEvent("test-tenant", "test-user", "target-user", "field", "old-value", "new-value").join();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Account updated event published", responseEntity.getBody().get("status"));
    }

    @Test
    public void testPublishAccountUpdatedEventError() {
        stubPublishFailure();
        ResponseEntity<Map<String, String>> responseEntity = controller.publishAccountUpdatedEvent("test-tenant", "test-user", "target-user", "field", "old-value", "new-value").join();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Kafka unavailable", responseEntity.getBody().get("error"));
    }

    @SuppressWarnings("unchecked")
    private void stubPublishSuccess() {
        when(publisher.publish(any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
    }

    private void stubPublishFailure() {
        when(publisher.publish(any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka unavailable")));
    }
}
