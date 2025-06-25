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
import java.util.concurrent.atomic.AtomicBoolean;

public class JpaCacheGetWithLoaderTest extends AbstractJpaCacheIntegrationTest {

    private static final String CACHE_NAME = "jpa:cache";

    @Override
    protected Class<?> getTestConfigClass() {
        return CacheConfig.class;
    }

    @Test
    void testGetWithValueLoader() {
        Cache cache = cacheManager().getCache(CACHE_NAME);
        Assertions.assertNotNull(cache);

        // GIVEN
        AtomicBoolean loaderCalled = new AtomicBoolean(false);

        // WHEN
        String first = cache.get("missingKey", () -> {
            loaderCalled.set(true);
            return "computedValue";
        });

        // THEN
        Assertions.assertTrue(loaderCalled.get());
        Assertions.assertEquals("computedValue", first);

        // WHEN
        loaderCalled.set(false);
        String second = cache.get("missingKey", () -> "otherValue");

        // THEN
        Assertions.assertFalse(loaderCalled.get());
        Assertions.assertEquals("computedValue", second);
    }

    @Configuration
    static class CacheConfig {
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
