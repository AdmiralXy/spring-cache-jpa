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

public class JpaCacheLfuEvictionTest extends AbstractJpaCacheIntegrationTest {

    private static final String CACHE_NAME = "jpa:cache";

    @Override
    protected Class<?> getTestConfigClass() {
        return LfuTestConfig.class;
    }

    @Test
    void testLfuEviction() throws InterruptedException {
        Cache cache = cacheManager().getCache(CACHE_NAME);
        Assertions.assertNotNull(cache);

        // GIVEN
        cache.put("key1", "value1");
        Assertions.assertEquals("value1", cache.get("key1", String.class));
        Assertions.assertEquals("value1", cache.get("key1", String.class));

        // WHEN
        cache.put("key2", "value2");
        Thread.sleep(2500);

        // THEN
        Assertions.assertEquals("value1", cache.get("key1", String.class));
        Assertions.assertNull(cache.get("key2"));
    }

    @Configuration
    static class LfuTestConfig {
        @Bean
        JpaCacheSettings settings() {
            return JpaCacheSettings.builder()
                    .evictionPolicy(EvictionPolicy.LFU)
                    .maxSize(1)
                    .ttl(Duration.ofMinutes(5))
                    .leaderMaintenanceInterval(Duration.ofSeconds(1))
                    .build();
        }
    }
}
