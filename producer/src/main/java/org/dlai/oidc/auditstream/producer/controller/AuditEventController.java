package org.dlai.oidc.auditstream.producer.controller;

import org.dlai.oidc.auditstream.producer.service.AuditEventPublisher;
import org.dlai.oidc.auditstream.proto.audit.v1.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/audit")
public class AuditEventController {

    private final AuditEventPublisher publisher;

    public AuditEventController(AuditEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<Map<String, String>>> publishLoginEvent(
            @RequestParam String tenantId,
            @RequestParam String userId,
            @RequestParam String ipAddress,
            @RequestParam(defaultValue = "Mozilla/5.0") String userAgent,
            @RequestParam(defaultValue = "SUCCESS") String result
    ) {

        LoginResult loginResult = switch (result.toUpperCase()) {
            case "FAILURE" -> LoginResult.LOGIN_RESULT_FAILURE;
            case "LOCKED" -> LoginResult.LOGIN_RESULT_LOCKED;
            default -> LoginResult.LOGIN_RESULT_SUCCESS;
        };

        LoginEvent loginEvent = LoginEvent.newBuilder()
                .setUserId(userId)
                .setIpAddress(ipAddress)
                .setUserAgent(userAgent)
                .setResult(loginResult)
                .build();

        AuditEventEnvelope.Builder envelopeBuilder = AuditEventEnvelope.newBuilder()
                .setTenantId(tenantId)
                .setActorId(userId)
                .setLoginEvent(loginEvent);

        return publisher.publish(envelopeBuilder).handle((pubResult, error) -> {
            if (error != null)
                return ResponseEntity.internalServerError().body(Map.of("error", error.getMessage()));
            else
                return ResponseEntity.ok().body(Map.of("status", "Login event published"));
        });
    }

    @PostMapping("/permission")
    public CompletableFuture<ResponseEntity<Map<String, String>>> publishPermissionChangedEvent(
            @RequestParam String tenantId,
            @RequestParam String userId,
            @RequestParam String targetUserId,
            @RequestParam String permission,
            @RequestParam(defaultValue = "GRANTED") String changeType,
            @RequestParam(required = false) String reason
    ) {
        ChangeType change = changeType.equalsIgnoreCase("GRANTED") ?
                ChangeType.CHANGE_TYPE_GRANTED : ChangeType.CHANGE_TYPE_REVOKED;

        PermissionChangedEvent.Builder permissionChangedEvent = PermissionChangedEvent.newBuilder()
                .setTargetUserId(targetUserId)
                .setPermission(permission)
                .setChangeType(change)
                .setChangedBy(userId);

        if (reason != null)
            permissionChangedEvent.setReason(reason);

        AuditEventEnvelope.Builder envelopeBuilder = AuditEventEnvelope.newBuilder()
                .setTenantId(tenantId)
                .setActorId(userId)
                .setPermissionChangedEvent(permissionChangedEvent.build());
        return publisher.publish(envelopeBuilder).handle((result, error) -> {
            if (error != null)
                return ResponseEntity.internalServerError().body(Map.of("error", error.getMessage()));
            else
                return ResponseEntity.ok().body(Map.of("status", "Permission changed event published"));
        });
    }

    @PostMapping("/account")
    public CompletableFuture<ResponseEntity<Map<String, String>>> publishAccountUpdatedEvent(
            @RequestParam String tenantId,
            @RequestParam String userId,
            @RequestParam String targetUserId,
            @RequestParam String fieldName,
            @RequestParam String originalValue,
            @RequestParam String newValue
    ) {
        AccountUpdatedEvent accountUpdatedEvent = AccountUpdatedEvent.newBuilder()
                .setTargetUserId(targetUserId)
                .setFieldName(fieldName)
                .setOriginalValue(originalValue)
                .setNewValue(newValue)
                .build();

        AuditEventEnvelope.Builder envelopeBuilder = AuditEventEnvelope.newBuilder()
                .setTenantId(tenantId)
                .setActorId(userId)
                .setAccountUpdatedEvent(accountUpdatedEvent);

        return publisher.publish(envelopeBuilder).handle((result, error) -> {
            if (error != null)
                return ResponseEntity.internalServerError().body(Map.of("error", error.getMessage()));
            else
                return ResponseEntity.ok().body(Map.of("status", "Account updated event published"));
        });

    }
}
