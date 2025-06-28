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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JpaCacheThreadSafetyTest extends AbstractJpaCacheIntegrationTest {

    private static final String CACHE_NAME = "jpa:cache";

    @Override
    protected Class<?> getTestConfigClass() {
        return ThreadSafetyTestConfig.class;
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        Cache cache = cacheManager().getCache(CACHE_NAME);
        Assertions.assertNotNull(cache);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        // WHEN
        for (int i = 0; i < 10; i++) {
            final String key = "key" + i;
            executor.submit(() -> {
                cache.put(key, "value" + key);
                Assertions.assertEquals("value" + key, cache.get(key, String.class));
                cache.evict(key);
                latch.countDown();
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // THEN
        for (int i = 0; i < 10; i++) {
            Assertions.assertNull(cache.get("key" + i));
        }
    }

    @Configuration
    static class ThreadSafetyTestConfig {
        @Bean
        JpaCacheSettings settings() {
            return JpaCacheSettings.builder()
                    .evictionPolicy(EvictionPolicy.FIFO)
                    .maxSize(100)
                    .ttl(Duration.ofMinutes(5))
                    .leaderMaintenanceInterval(Duration.ofMinutes(5))
                    .build();
        }
    }
}
