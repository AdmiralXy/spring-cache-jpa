package io.github.admiralxy.cache.jpa.demosb3.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.admiralxy.cache.jpa.api.CacheSerializer;
import io.github.admiralxy.cache.jpa.core.JpaCacheManager;
import io.github.admiralxy.cache.jpa.core.JpaCacheSettings;
import io.github.admiralxy.cache.jpa.core.serializer.JacksonSerializer;
import io.github.admiralxy.cache.jpa.core.task.LeaderTtlCleaner;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EntityScan({"io.github.admiralxy.cache.jpa.api.entity"})
public class JpaCacheConfiguration {

    /** Caffeine in-memory cache configuration. **/

    @Bean
    public Caffeine caffeineConfig() {
        return Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES);
    }

    @Bean
    public CaffeineCacheManager caffeineCacheManager(Caffeine caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }

    /** JDBC cache configuration. **/

    @Bean
    public CacheSerializer cacheSerializer(ObjectMapper mapper) {
        return new JacksonSerializer(mapper);
    }

    @Bean
    public JpaCacheSettings jdbcCacheSettings() {
        return JpaCacheSettings.builder()
                .maxSize(10_000)
                .ttl(Duration.ofMinutes(10))
                .build();
    }

    @Bean
    public JpaCacheManager jdbcCacheManager(EntityManager entityManager,
                                            PlatformTransactionManager platformTransactionManager,
                                            CacheSerializer serializer,
                                            JpaCacheSettings settings) {
        return new JpaCacheManager(entityManager, platformTransactionManager, serializer, settings);
    }

    @Bean(destroyMethod = "close")
    public LeaderTtlCleaner leaderTtlCleaner(EntityManager entityManager,
                                             PlatformTransactionManager platformTransactionManager,
                                             JpaCacheManager cacheManager,
                                             JpaCacheSettings settings) {
        return new LeaderTtlCleaner(entityManager, platformTransactionManager, cacheManager, settings);
    }

    /** Composite CacheManager that combines both Caffeine and JDBC cache managers. **/

    @Primary
    @Bean
    public CacheManager cacheManager(JpaCacheManager jpaCacheManager, CaffeineCacheManager caffeineCacheManager) {
        return new CompositeCacheManager(
                jpaCacheManager,
                caffeineCacheManager
        );
    }
}
