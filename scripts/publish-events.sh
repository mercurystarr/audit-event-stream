#!/usr/bin/env bash
# Publishes a sequence of audit events through the producer REST API.
# Run after `docker compose up` to verify the full pipeline end to end.
# Watch the consumer logs alongside: docker compose logs -f consumer

HOST=${1:-localhost:8081}

echo "Publishing audit events to $HOST..."
echo ""

echo "1. Successful login"
curl -s -X POST "$HOST/audit/login" \
  -d "tenantId=acme-corp&userId=alice&ipAddress=203.0.113.10&result=SUCCESS" | jq .

echo ""
echo "2. Failed login (credential stuffing simulation)"
curl -s -X POST "$HOST/audit/login" \
  -d "tenantId=acme-corp&userId=alice&ipAddress=198.51.100.99&result=FAILURE" | jq .

echo ""
echo "3. Permission grant (high-signal event for IAM audit)"
curl -s -X POST "$HOST/audit/permission" \
  -d "tenantId=acme-corp&userId=bob-admin&targetUserId=alice&permission=roles/admin&changeType=GRANTED&reason=Promotion+to+team+lead" | jq .

echo ""
echo "4. Account email update"
curl -s -X POST "$HOST/audit/account" \
  -d "tenantId=acme-corp&userId=alice&targetUserId=alice&fieldName=email&originalValue=alice@old.com&newValue=alice@acme.com" | jq .

echo ""
echo "Done. Check consumer logs: docker compose logs -f consumer"
