package org.dlai.oidc.auditstream.consumer.handler;

import org.dlai.oidc.auditstream.consumer.domain.AuditEvent;
import org.dlai.oidc.auditstream.proto.audit.v1.AuditEventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AuditEventHandler.class);

    public void handle(AuditEventEnvelope envelope) {
        handle(AuditEvent.from(envelope));
    }

    private void handle(AuditEvent event) {
        switch (event) {
            case AuditEvent.LoginOccurred(
                    var eventId, var tenantId, var userId, var ipAddress, var result) ->
                    LOG.info("LOGIN eventId={} tenantId={} userId={} result={} ip={}",
                            eventId, tenantId, userId, result, ipAddress);
            case AuditEvent.PermissionChanged(
                    var eventId, var tenantId, var targetUserId, var permission,
                    var changeType, var changedBy, var reason) ->
                    LOG.info("PERMISSION CHANGE eventId={} tenantId={} targetUserId={} permission={} changeType={} changedBy={} reason={}",
                            eventId, tenantId, targetUserId, permission, changeType, changedBy, reason);
            case AuditEvent.AccountUpdated(
                    var eventId, var tenantId, var targetUserId, var fieldName,
                    var originalValue, var newValue) ->
                    LOG.info("ACCOUNT UPDATE eventId={} tenantId={} targetUserId={} fieldName={} originalValue={} newValue={}",
                            eventId, tenantId, targetUserId, fieldName, originalValue, newValue);
        }
    }
}