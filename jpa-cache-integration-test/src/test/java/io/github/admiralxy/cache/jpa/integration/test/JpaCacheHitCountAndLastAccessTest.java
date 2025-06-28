package io.github.admiralxy.cache.jpa.integration.test;

import io.github.admiralxy.cache.jpa.api.EvictionPolicy;
import io.github.admiralxy.cache.jpa.api.entity.entry.JpaCacheEntity;
import io.github.admiralxy.cache.jpa.core.JpaCacheSettings;
import io.github.admiralxy.cache.jpa.integration.test.base.AbstractJpaCacheIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;

public class JpaCacheHitCountAndLastAccessTest extends AbstractJpaCacheIntegrationTest {

    private static final String CACHE_NAME = "jpa:cache";
    private static final String JPQL_SELECT = "SELECT e FROM JpaCacheEntity e WHERE e.id.name = :name AND e.id.key = :key";

    private static final String KEY = "hitKey";
    private static final String VALUE = "value";

    @Override
    protected Class<?> getTestConfigClass() {
        return HitsTestConfig.class;
    }

    @Test
    void testHitCountAndLastAccess() throws InterruptedException {
        // GIVEN
        Cache cache = cacheManager().getCache(CACHE_NAME);
        Assertions.assertNotNull(cache);

        EntityManager em = ctx.getBean(EntityManager.class);
        TransactionTemplate tx = ctx.getBean(TransactionTemplate.class);

        cache.put(KEY, VALUE);
        JpaCacheEntity before = tx.execute(status -> {
            em.flush();
            em.clear();
            return em.createQuery(JPQL_SELECT, JpaCacheEntity.class)
                    .setParameter("name", CACHE_NAME)
                    .setParameter("key", KEY)
                    .getSingleResult();
        });
        long hitsBefore = before.getHitCount();
        Instant lastAccessBefore = before.getLastAccessedAt();

        // WHEN
        Thread.sleep(500);
        String value = cache.get(KEY, String.class);

        // THEN
        Assertions.assertEquals(VALUE, value);

        JpaCacheEntity after = tx.execute(status -> {
            em.flush();
            em.clear();
            return em.createQuery(JPQL_SELECT, JpaCacheEntity.class)
                    .setParameter("name", CACHE_NAME)
                    .setParameter("key", KEY)
                    .getSingleResult();
        });
        Assertions.assertNotNull(after);
        Assertions.assertEquals(hitsBefore + 1, after.getHitCount());
        Assertions.assertTrue(after.getLastAccessedAt().isAfter(lastAccessBefore));
    }

    @Configuration
    static class HitsTestConfig {
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
