package io.github.admiralxy.cache.jpa.integration.test;

import io.github.admiralxy.cache.jpa.api.EvictionPolicy;
import io.github.admiralxy.cache.jpa.core.JpaCacheSettings;
import io.github.admiralxy.cache.jpa.integration.test.base.AbstractJpaCacheIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

public class JpaCacheEvictClearTest extends AbstractJpaCacheIntegrationTest {

    private static final String CACHE_NAME = "jpa:cache";

    @Override
    protected Class<?> getTestConfigClass() {
        return EvictClearTestConfig.class;
    }

    @Test
    void testEvict() {
        Cache cache = cacheManager().getCache(CACHE_NAME);
        Assertions.assertNotNull(cache);

        // GIVEN
        cache.put("a", "A");
        Assertions.assertEquals("A", cache.get("a", String.class));

        // WHEN
        cache.evict("a");

        // THEN
        Assertions.assertNull(cache.get("a"));
    }

    @Test
    void testClear() {
        Cache cache = cacheManager().getCache(CACHE_NAME);
        Assertions.assertNotNull(cache);

        // GIVEN
        cache.put("x", "X");
        cache.put("y", "Y");
        Assertions.assertEquals("X", cache.get("x", String.class));
        Assertions.assertEquals("Y", cache.get("y", String.class));

        // WHEN
        cache.clear();

        // THEN
        Assertions.assertNull(cache.get("x"));
        Assertions.assertNull(cache.get("y"));
    }

    @Configuration
    static class EvictClearTestConfig {
        @Bean
        JpaCacheSettings settings() {
            return JpaCacheSettings.builder()
                    .evictionPolicy(EvictionPolicy.FIFO)
                    .maxSize(10)
                    .ttl(Duration.ofMinutes(5))
                    .leaderMaintenanceInterval(Duration.ofSeconds(1))
                    .build();
        }
    }
}
