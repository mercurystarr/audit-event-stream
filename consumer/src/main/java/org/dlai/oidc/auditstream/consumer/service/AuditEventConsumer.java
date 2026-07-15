package org.dlai.oidc.auditstream.consumer.service;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.dlai.oidc.auditstream.consumer.handler.AuditEventHandler;
import org.dlai.oidc.auditstream.consumer.idempotency.IdempotencyStore;
import org.dlai.oidc.auditstream.proto.audit.v1.AuditEventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class AuditEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(AuditEventConsumer.class);
    private final IdempotencyStore idempotencyStore;
    private final AuditEventHandler handler;

    public AuditEventConsumer(IdempotencyStore idempotencyStore, AuditEventHandler handler) {
        this.idempotencyStore = idempotencyStore;
        this.handler = handler;
    }

    @KafkaListener(topics = "${audit.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, byte[]> record) throws InvalidProtocolBufferException {
        LOG.debug("received record topic={} partition={} offset={}", record.topic(), record.partition(), record.offset());

        AuditEventEnvelope envelope = AuditEventEnvelope.parseFrom(record.value());
        if (!idempotencyStore.markProcessed(envelope.getEventId())) {
            return;
        }
        handler.handle(envelope);
    }

    @KafkaListener(topics = "${audit.kafka.topic}.DLT", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeDlt(ConsumerRecord<String, byte[]> record,
                           @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage) {
        LOG.error("message routed to DLT topic={} partition={} offset={} error={}",
                record.topic(), record.partition(), record.offset(), exceptionMessage);
    }

}
