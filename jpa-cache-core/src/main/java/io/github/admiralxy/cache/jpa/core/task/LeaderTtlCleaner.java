package io.github.admiralxy.cache.jpa.core.task;

import io.github.admiralxy.cache.jpa.core.JpaCache;
import io.github.admiralxy.cache.jpa.core.JpaCacheManager;
import io.github.admiralxy.cache.jpa.core.JpaCacheSettings;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public final class LeaderTtlCleaner implements Runnable, AutoCloseable {

    private static final String JPQL_ACQUIRE =
            "UPDATE JpaCacheLeaderEntity l " +
                    "SET l.instanceId = :instanceId, l.leaseUntil = :leaseUntil " +
                    "WHERE l.id = 1 AND (l.leaseUntil < :now OR l.instanceId IS NULL)";

    private static final String JPQL_RENEW =
            "UPDATE JpaCacheLeaderEntity l " +
                    "SET l.leaseUntil = :leaseUntil " +
                    "WHERE l.id = 1 AND l.instanceId = :instanceId";

    private static final String JPQL_RELEASE =
            "UPDATE JpaCacheLeaderEntity l " +
                    "SET l.instanceId = NULL, l.leaseUntil = :epoch " +
                    "WHERE l.id = 1 AND l.instanceId = :instanceId";

    private static final String JPQL_CLEAN_EXPIRED =
            "DELETE FROM JpaCacheEntity e WHERE e.expiresAt < :now";

    private static final String THREAD_PREFIX = "spring-cache-jpa-leader-cleaner-";
    private static final AtomicInteger NAME_SEQ = new AtomicInteger();

    private final EntityManager em;
    private final TransactionTemplate txTemplate;
    private final JpaCacheManager cacheManager;
    private final Duration leaseLength;
    private final Duration renewInterval;
    private final Duration backoffMin;
    private final Duration backoffMax;

    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isLeader = new AtomicBoolean(false);
    private final String instanceId = UUID.randomUUID().toString();
    private final AtomicReference<Duration> backoff = new AtomicReference<>(Duration.ZERO);

    public LeaderTtlCleaner(EntityManager em,
                            PlatformTransactionManager txManager,
                            JpaCacheManager cacheManager,
                            JpaCacheSettings settings) {
        this.em = em;
        this.txTemplate = new TransactionTemplate(txManager);
        this.txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        this.txTemplate.setTimeout(10);
        this.txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        this.cacheManager = cacheManager;
        this.leaseLength = settings.getLeaderLeaseDuration();
        this.renewInterval = settings.getLeaderMaintenanceInterval();
        this.backoffMin = settings.getBackoffMin();
        this.backoffMax = settings.getBackoffMax();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new CleanerThreadFactory());

        scheduleNext(Duration.ZERO);
    }

    @Override
    public void run() {
        Instant now = Instant.now();
        Duration nextDelay;
        try {
            if (!isLeader.get()) {
                nextDelay = acquireWithLock(now);
            } else {
                nextDelay = renewWithLock(now);
            }
        } catch (Exception e) {
            log.error("Cleaner tick error: {}", e.toString());
            nextDelay = backoffMin;
        }
        try {
            scheduleNext(nextDelay);
        } catch (RejectedExecutionException e) {
            log.warn("Failed to schedule next cleaner run: {}", e.toString());
        }
    }

    @Override
    public void close() {
        try {
            txTemplate.execute(status -> {
                Instant epoch = Instant.ofEpochSecond(1);
                Query q = em.createQuery(JPQL_RELEASE);
                q.setParameter("epoch", epoch);
                q.setParameter("instanceId", instanceId);
                q.executeUpdate();
                log.debug("Released leadership for instance {}", instanceId);
                return null;
            });
        } catch (Exception e) {
            log.warn("Failed to release leadership: {}", e, e);
        } finally {
            scheduler.shutdownNow();
        }
    }

    private void scheduleNext(Duration delay) {
        scheduler.schedule(this, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private Duration acquireWithLock(Instant now) {
        return txTemplate.execute(status -> {
            Instant leaseTo = now.plus(leaseLength);

            Query q = em.createQuery(JPQL_ACQUIRE);
            q.setParameter("instanceId", instanceId);
            q.setParameter("leaseUntil", leaseTo);
            q.setParameter("now", now);

            int rows = q.executeUpdate();
            if (rows == 1) {
                isLeader.set(true);
                backoff.set(Duration.ZERO);
                log.debug("Acquired leadership for {} ms", renewInterval.toMillis());
                return renewInterval;
            }
            Duration updated = backoff.updateAndGet(this::nextBackoff);
            return jitter(updated);
        });
    }

    private Duration renewWithLock(Instant now) {
        return txTemplate.execute(status -> {
            Instant leaseTo = now.plus(leaseLength);

            Query q = em.createQuery(JPQL_RENEW);
            q.setParameter("leaseUntil", leaseTo);
            q.setParameter("instanceId", instanceId);
            int rows = q.executeUpdate();

            if (rows == 1) {
                em.createQuery(JPQL_CLEAN_EXPIRED)
                        .setParameter("now", now)
                        .executeUpdate();

                log.debug("Renewed leadership until {}", leaseTo);
                for (String cacheName : cacheManager.getCacheNames()) {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache instanceof JpaCache) {
                        ((JpaCache) cache).enforceMaxSize();
                    }
                }
                return renewInterval;
            } else {
                isLeader.set(false);
                backoff.set(backoffMin);
                log.debug("Lost leadership");
                return backoffMin;
            }
        });
    }

    private Duration nextBackoff(Duration current) {
        if (current.isZero()) {
            return backoffMin;
        }
        Duration doubled = current.multipliedBy(2);
        return doubled.compareTo(backoffMax) > 0 ? backoffMax : doubled;
    }

    private static Duration jitter(Duration d) {
        double k = 0.9 + ThreadLocalRandom.current().nextDouble() * 0.2;
        return Duration.ofMillis(Math.round(d.toMillis() * k));
    }

    private static final class CleanerThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread t = new Thread(r, THREAD_PREFIX + NAME_SEQ.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }
}
