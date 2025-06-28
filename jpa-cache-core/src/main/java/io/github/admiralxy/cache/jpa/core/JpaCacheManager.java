package io.github.admiralxy.cache.jpa.core;

import io.github.admiralxy.cache.jpa.api.CacheSerializer;
import io.github.admiralxy.cache.jpa.core.eviction.EvictionSelector;
import io.github.admiralxy.cache.jpa.core.eviction.impl.FifoSelector;
import io.github.admiralxy.cache.jpa.core.eviction.impl.LfuSelector;
import io.github.admiralxy.cache.jpa.core.eviction.impl.LruSelector;
import jakarta.persistence.EntityManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JpaCacheManager implements CacheManager {

    private final Map<String, Cache> caches = new ConcurrentHashMap<>();
    private final EntityManager em;
    private final TransactionTemplate txTemplate;
    private final CacheSerializer serializer;
    private final EvictionSelector selector;
    private final JpaCacheSettings settings;

    public JpaCacheManager(EntityManager em, PlatformTransactionManager txManager, CacheSerializer serializer, JpaCacheSettings settings) {
        this.em = em;
        this.txTemplate = new TransactionTemplate(txManager);
        this.serializer = serializer;
        this.settings = settings;
        this.selector = switch (settings.getEvictionPolicy()) {
            case FIFO -> new FifoSelector(em);
            case LFU -> new LfuSelector(em);
            case LRU -> new LruSelector(em);
        };
    }

    @Override
    public Cache getCache(String name) {
        if (!name.startsWith(settings.getPrefix())) {
            return null;
        }
        return caches.computeIfAbsent(name, n -> new JpaCache(n, em, txTemplate, serializer, selector, settings));
    }

    @NonNull
    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(caches.keySet());
    }
}
