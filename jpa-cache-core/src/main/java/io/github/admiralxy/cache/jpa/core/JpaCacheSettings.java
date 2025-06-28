package io.github.admiralxy.cache.jpa.core;

import io.github.admiralxy.cache.jpa.api.EvictionPolicy;
import lombok.Builder;
import lombok.Value;

import java.time.Duration;

/**
 * Mutable settings holder.
 */
@Value
@Builder
public class JpaCacheSettings {

    /**
     * The eviction policy to use.
     */
    @Builder.Default
    EvictionPolicy evictionPolicy = EvictionPolicy.LRU;

    /**
     * The maximum size.
     */
    @Builder.Default
    int maxSize = 10_000;

    /**
     * The time-to-live.
     */
    @Builder.Default
    Duration ttl = Duration.ofMinutes(5);

    /**
     * The prefix for the cache keys.
     */
    @Builder.Default
    String prefix = "jpa:";

    /**
     * Leader lease duration.
     */
    @Builder.Default
    Duration leaderLeaseDuration = Duration.ofSeconds(120);

    /**
     * Interval at which the leader renews its lease and purges expired entries.
     */
    @Builder.Default
    Duration leaderMaintenanceInterval = Duration.ofSeconds(60);

    /**
     * Minimum backoff duration for leader election retries.
     */
    @Builder.Default
    Duration backoffMin = Duration.ofSeconds(60);

    /**
     * Maximum backoff duration for leader election retries.
     */
    @Builder.Default
    Duration backoffMax = Duration.ofSeconds(240);
}
