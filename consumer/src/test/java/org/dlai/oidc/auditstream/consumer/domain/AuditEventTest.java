package org.dlai.oidc.auditstream.consumer.domain;

import org.dlai.oidc.auditstream.proto.audit.v1.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuditEventTest {

    @Test
    public void testFromWithLoginEvent() {
        LoginEvent event = loginEvent();
        AuditEventEnvelope envelope = auditEventEnvelope()
                .setLoginEvent(event)
                .build();

        AuditEvent.LoginOccurred expected = new AuditEvent.LoginOccurred(
                "event-id",
                "tenant-id",
                "user-id",
                "192.168.1.1",
                LoginResult.LOGIN_RESULT_SUCCESS);

        AuditEvent result = AuditEvent.from(envelope);
        assertEquals(expected, result);
    }

    @Test
    public void testFromWithPermissionChangedEvent() {
        PermissionChangedEvent event = permissionChangedEvent();
        AuditEventEnvelope envelope = auditEventEnvelope()
                .setPermissionChangedEvent(event)
                .build();

        AuditEvent.PermissionChanged expected = new AuditEvent.PermissionChanged(
                "event-id",
                "tenant-id",
                "target-user-id",
                "permission",
                ChangeType.CHANGE_TYPE_GRANTED,
                "user-id",
                "reason");

        AuditEvent result = AuditEvent.from(envelope);
        assertEquals(expected, result);
    }

    @Test
    public void testFromWithAccountUpdatedEvent() {
        AccountUpdatedEvent event = accountUpdatedEvent();
        AuditEventEnvelope envelope = auditEventEnvelope()
                .setAccountUpdatedEvent(event)
                .build();

        AuditEvent.AccountUpdated expected = new AuditEvent.AccountUpdated(
                "event-id",
                "tenant-id",
                "target-user-id",
                "field-name",
                "original-value",
                "new-value");

        AuditEvent result = AuditEvent.from(envelope);
        assertEquals(expected, result);
    }

    @Test
    public void testFromWithPayloadNotSet() {
        AuditEventEnvelope envelope = auditEventEnvelope().build();

        assertThrows(IllegalArgumentException.class, () -> AuditEvent.from(envelope));
    }

    private AuditEventEnvelope.Builder auditEventEnvelope() {
        return AuditEventEnvelope.newBuilder()
                .setEventId("event-id")
                .setTenantId("tenant-id")
                .setActorId("user-id")
                .setOccurredAt("occurred-at");
    }

    private LoginEvent loginEvent() {
        return LoginEvent.newBuilder()
                .setUserId("user-id")
                .setIpAddress("192.168.1.1")
                .setUserAgent("user-agent")
                .setResult(LoginResult.LOGIN_RESULT_SUCCESS)
                .build();
    }

    private PermissionChangedEvent permissionChangedEvent() {
        return PermissionChangedEvent.newBuilder()
                .setTargetUserId("target-user-id")
                .setPermission("permission")
                .setChangeType(ChangeType.CHANGE_TYPE_GRANTED)
                .setChangedBy("user-id")
                .setReason("reason")
                .build();

    }

    private AccountUpdatedEvent accountUpdatedEvent() {
        return AccountUpdatedEvent.newBuilder()
                .setTargetUserId("target-user-id")
                .setFieldName("field-name")
                .setOriginalValue("original-value")
                .setNewValue("new-value")
                .build();
    }
}
