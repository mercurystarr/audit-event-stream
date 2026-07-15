package org.dlai.oidc.auditstream.consumer.domain;

import org.dlai.oidc.auditstream.proto.audit.v1.*;

public sealed interface AuditEvent permits AuditEvent.LoginOccurred, AuditEvent.PermissionChanged, AuditEvent.AccountUpdated {

    static AuditEvent from(AuditEventEnvelope envelope) {
        return switch (envelope.getPayloadCase()) {
            case LOGIN_EVENT -> {
                LoginEvent event = envelope.getLoginEvent();
                yield new LoginOccurred(envelope.getEventId(), envelope.getTenantId(),
                        event.getUserId(), event.getIpAddress(), event.getResult());
            }
            case PERMISSION_CHANGED_EVENT -> {
                PermissionChangedEvent event = envelope.getPermissionChangedEvent();
                yield new PermissionChanged(envelope.getEventId(), envelope.getTenantId(),
                        event.getTargetUserId(), event.getPermission(), event.getChangeType(),
                        event.getChangedBy(), event.getReason());
            }
            case ACCOUNT_UPDATED_EVENT -> {
                AccountUpdatedEvent event = envelope.getAccountUpdatedEvent();
                yield new AccountUpdated(envelope.getEventId(), envelope.getTenantId(),
                        event.getTargetUserId(), event.getFieldName(), event.getOriginalValue(),
                        event.getNewValue());
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("envelope has no payload set: " + envelope.getEventId());
        };
    }

    record LoginOccurred(String eventId, String tenantId, String userId, String ipAddress, LoginResult result) implements AuditEvent {}
    record PermissionChanged(String eventId, String tenantId, String targetUserId, String permission, ChangeType changeType, String changedBy, String reason) implements AuditEvent { }
    record AccountUpdated(String eventId, String tenantId, String targetUserId, String fieldName, String originalValue, String newValue) implements AuditEvent {}

}

