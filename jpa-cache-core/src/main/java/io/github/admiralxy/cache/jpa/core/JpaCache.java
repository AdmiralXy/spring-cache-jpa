package io.github.admiralxy.cache.jpa.core;

import io.github.admiralxy.cache.jpa.api.CacheException;
import io.github.admiralxy.cache.jpa.api.CacheSerializer;
import io.github.admiralxy.cache.jpa.api.entity.entry.JpaCacheEntity;
import io.github.admiralxy.cache.jpa.api.entity.entry.JpaCacheEntityId;
import io.github.admiralxy.cache.jpa.core.eviction.EvictionSelector;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
public class JpaCache implements Cache {

    private static final String JPQL_DELETE_ALL =
            "DELETE FROM JpaCacheEntity e WHERE e.id.name = :cacheName";

    private static final String JPQL_DELETE_ONE =
            "DELETE FROM JpaCacheEntity e WHERE e.id.name = :cacheName AND e.id.key = :cacheKey";

    private static final String JPQL_COUNT =
            "SELECT COUNT(e) FROM JpaCacheEntity e WHERE e.id.name = :cacheName";

    private final String name;
    private final EntityManager em;
    private final TransactionTemplate txTemplate;
    private final CacheSerializer serializer;
    private final EvictionSelector selector;
    private final JpaCacheSettings settings;

    public JpaCache(String name,
                    EntityManager em,
                    TransactionTemplate txTemplate,
                    CacheSerializer serializer,
                    EvictionSelector selector,
                    JpaCacheSettings settings) {
        this.name = name;
        this.em = em;
        this.txTemplate = txTemplate;
        this.serializer = serializer;
        this.selector = selector;
        this.settings = settings;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        Object val = get(key, Object.class);
        return val != null ? new SimpleValueWrapper(val) : null;
    }

    @Override
    public <T> T get(@NonNull Object key, @Nullable Class<T> type) {
        return txTemplate.execute(status -> {
            JpaCacheEntityId id = new JpaCacheEntityId(name, key.toString());
            JpaCacheEntity entry = em.find(JpaCacheEntity.class, id);
            if (entry == null) {
                return null;
            }
            Instant now = Instant.now();
            if (entry.getExpiresAt().isBefore(now)) {
                em.remove(entry);
                return null;
            }
            entry.setLastAccessedAt(now);
            entry.setHitCount(entry.getHitCount() + 1);
            em.merge(entry);

            byte[] data = entry.getPayload();
            try {
                assert type != null;
                return serializer.deserialize(data, type);
            } catch (Exception e) {
                throw new CacheException("Failed to deserialize cache entry", e);
            }
        });
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            @SuppressWarnings("unchecked")
            T val = (T) wrapper.get();
            return val;
        }
        try {
            T loaded = valueLoader.call();
            put(key, loaded);
            return loaded;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(@NonNull Object key, @Nullable Object value) {
        txTemplate.execute(status -> {
            if (value == null) {
                em.createQuery(JPQL_DELETE_ONE)
                        .setParameter("cacheName", name)
                        .setParameter("cacheKey", key.toString())
                        .executeUpdate();
                return null;
            }

            byte[] raw;
            try {
                raw = serializer.serialize(value);
            } catch (Exception e) {
                throw new CacheException("Failed to serialize cache value", e);
            }
            Instant now = Instant.now();
            Instant expiresAt = now.plus(settings.getTtl());

            JpaCacheEntityId id = new JpaCacheEntityId(name, key.toString());
            JpaCacheEntity entry = em.find(JpaCacheEntity.class, id);
            if (entry == null) {
                entry = new JpaCacheEntity();
                entry.setId(id);
                entry.setCreatedAt(now);
                entry.setHitCount(0);
            }
            entry.setPayload(raw);
            entry.setExpiresAt(expiresAt);
            entry.setLastAccessedAt(now);
            em.merge(entry);
            return null;
        });
    }

    @Override
    public void evict(@NonNull Object key) {
        txTemplate.execute(status -> {
            JpaCacheEntityId id = new JpaCacheEntityId(name, key.toString());
            JpaCacheEntity entry = em.find(JpaCacheEntity.class, id);
            if (entry != null) {
                em.remove(entry);
            }
            return null;
        });
    }

    @Override
    public void clear() {
        txTemplate.execute(status -> {
            em.createQuery(JPQL_DELETE_ALL)
                    .setParameter("cacheName", name)
                    .executeUpdate();
            return null;
        });
    }

    public void enforceMaxSize() {
        txTemplate.execute(status -> {
            Long count = em.createQuery(JPQL_COUNT, Long.class)
                    .setParameter("cacheName", name)
                    .getSingleResult();
            int overflow = count.intValue() - settings.getMaxSize();
            if (overflow > 0) {
                List<String> victims = selector.selectVictims(name, overflow);
                for (String v : victims) {
                    JpaCacheEntityId vid = new JpaCacheEntityId(name, v);
                    JpaCacheEntity victim = em.find(JpaCacheEntity.class, vid);
                    if (victim != null) {
                        em.remove(victim);
                    }
                }
            }
            return null;
        });
    }
}
