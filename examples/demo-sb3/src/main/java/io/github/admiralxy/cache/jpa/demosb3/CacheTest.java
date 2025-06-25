package io.github.admiralxy.cache.jpa.demosb3;

import io.github.admiralxy.cache.jpa.demosb3.loader.CachedLoader;
import io.github.admiralxy.cache.jpa.demosb3.loader.InMemoryCachedLoader;
import io.github.admiralxy.cache.jpa.demosb3.loader.JpaCachedLoader;
import io.github.admiralxy.cache.jpa.demosb3.model.Holder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheTest {

    private final CacheManager cacheManager;
    private final JpaCachedLoader jpaCachedLoader;
    private final InMemoryCachedLoader inMemoryCachedLoader;

    public void getValueWithJpaCacheCheck(String id) {
        getValue(id, "jpa:testCache", jpaCachedLoader);
    }

    public void getValueWithInMemoryCacheCheck(String id) {
        getValue(id, "testCache", inMemoryCachedLoader);
    }

    private void getValue(String id, String cacheName, CachedLoader cachedLoader) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Holder cached = cache.get(id, Holder.class);
            if (cached != null) {
                log.info("✅ Cache hit for id = {}, cached value: {}", id, cached);
                return;
            }
        }

        cachedLoader.loadValue(id);
    }
}
