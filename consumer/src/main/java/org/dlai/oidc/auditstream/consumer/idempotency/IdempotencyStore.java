package org.dlai.oidc.auditstream.consumer.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class IdempotencyStore {

    private static final Logger LOG = LoggerFactory.getLogger(IdempotencyStore.class);
    private final StringRedisTemplate redisTemplate;
    private final Duration ttl = Duration.ofDays(7);

    public IdempotencyStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean markProcessed(String eventId) {
        String keyPrefix = "audit:seen:";
        Boolean result = redisTemplate.opsForValue().setIfAbsent(keyPrefix +eventId, "1", ttl);
        if (result == null) {
            LOG.warn("Redis is unavailable");
            return false;
        }
        if (!result) {
            LOG.info("Duplicate event detected: {}, skipping", eventId);
        }
        return result;
    }
}
