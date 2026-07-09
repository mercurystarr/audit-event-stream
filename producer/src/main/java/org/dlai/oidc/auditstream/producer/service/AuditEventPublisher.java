package org.dlai.oidc.auditstream.producer.service;

import org.dlai.oidc.auditstream.proto.audit.v1.AuditEventEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Publishes audit events to Kafka as Protobuf-serialized byte arrays.
 *
 * <p>Events are keyed by {@code tenant_id}. Kafka only guarantees message
 * ordering within a partition, not across a topic, so keying by tenant_id
 * ensures a given tenant's events are always processed in the order they
 * occurred (e.g. a permission change is seen after the login that preceded
 * it) — which matters for audit-trail correctness.
 */
@Service
public class AuditEventPublisher {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final String topic;

    @Autowired
    public AuditEventPublisher(KafkaTemplate<String, byte[]> kafkaTemplate,
                               @Value("${audit.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Publishes an envelope to Kafka. The caller is responsible for populating
     * the payload (login_event, permission_changed_event, etc.); this method
     * stamps the event_id and occurred_at before sending.
     */
    public CompletableFuture<SendResult<String, byte[]>> publish(AuditEventEnvelope.Builder envelopeBuilder) {
        AuditEventEnvelope envelope = envelopeBuilder
                .setEventId(UUID.randomUUID().toString())
                .setOccurredAt(Instant.now().toString())
                .build();

        byte[] payload = envelope.toByteArray();
        String partitionKey = envelope.getTenantId();

        return kafkaTemplate.send(topic, partitionKey, payload);
    }
}
