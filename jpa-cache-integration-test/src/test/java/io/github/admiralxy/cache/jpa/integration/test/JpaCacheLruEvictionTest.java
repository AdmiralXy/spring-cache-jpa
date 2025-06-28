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

public class JpaCacheLruEvictionTest extends AbstractJpaCacheIntegrationTest {

    private static final String CACHE_NAME = "jpa:cache";

    @Override
    protected Class<?> getTestConfigClass() {
        return LruTestConfig.class;
    }

    @Test
    void testLruEviction() throws InterruptedException {
        Cache cache = cacheManager().getCache(CACHE_NAME);
        Assertions.assertNotNull(cache);

        // GIVEN
        cache.put("key1", "value1");
        Assertions.assertEquals("value1", cache.get("key1", String.class));

        cache.put("key2", "value2");
        Assertions.assertEquals("value2", cache.get("key2", String.class));

        // WHEN
        cache.put("key3", "value3");
        Thread.sleep(2500);

        // THEN
        Assertions.assertNull(cache.get("key1"));
        Assertions.assertNull(cache.get("key2"));
        Assertions.assertEquals("value3", cache.get("key3", String.class));
    }

    @Configuration
    static class LruTestConfig {
        @Bean
        JpaCacheSettings settings() {
            return JpaCacheSettings.builder()
                    .evictionPolicy(EvictionPolicy.LRU)
                    .maxSize(1)
                    .ttl(Duration.ofMinutes(5))
                    .leaderMaintenanceInterval(Duration.ofSeconds(1))
                    .build();
        }
    }
}
