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

public class JpaCacheUpdateTest extends AbstractJpaCacheIntegrationTest {

    private static final String CACHE_NAME = "jpa:cache";

    @Override
    protected Class<?> getTestConfigClass() {
        return FifoTestConfig.class;
    }

    @Test
    void testUpdate() {
        Cache cache = cacheManager().getCache(CACHE_NAME);
        Assertions.assertNotNull(cache);

        // GIVEN
        cache.put("key1", "value1");

        // WHEN
        cache.put("key1", "value2");

        // THEN
        Assertions.assertEquals("value2", cache.get("key1", String.class));
    }

    @Configuration
    static class FifoTestConfig {
        @Bean
        JpaCacheSettings settings() {
            return JpaCacheSettings.builder()
                    .evictionPolicy(EvictionPolicy.FIFO)
                    .maxSize(1)
                    .ttl(Duration.ofMinutes(5))
                    .leaderMaintenanceInterval(Duration.ofSeconds(1))
                    .build();
        }
    }
}
