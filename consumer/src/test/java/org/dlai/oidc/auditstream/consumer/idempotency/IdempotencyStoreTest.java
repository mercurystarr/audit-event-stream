package org.dlai.oidc.auditstream.consumer.idempotency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IdempotencyStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    private IdempotencyStore store;

    private final String keyPrefix = "audit:seen:";
    private final Duration ttl = Duration.ofDays(7);

    @BeforeEach
    public void setUp() {
        store = new IdempotencyStore(redisTemplate);
    }

    @Test
    public void testMarkProcessed() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(keyPrefix + "test-event-id", "1", ttl)).thenReturn(true);
        boolean result = store.markProcessed("test-event-id");
        assertTrue(result);
    }

    @Test
    public void testMarkProcessedDuplicate() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(keyPrefix + "test-event-id", "1", ttl)).thenReturn(false);
        boolean result = store.markProcessed("test-event-id");
        assertFalse(result);
    }

    @Test
    public void testMarkProcessedRedisUnavailable() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(keyPrefix + "test-event-id", "1", ttl)).thenReturn(null);
        boolean result = store.markProcessed("test-event-id");
        assertFalse(result);
    }
}
