package io.github.admiralxy.cache.jpa.demosb3.loader;

import io.github.admiralxy.cache.jpa.demosb3.model.Holder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InMemoryCachedLoader implements CachedLoader {

    @Override
    @Cacheable(value = "testCache", key = "#id")
    public void loadValue(String id) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Holder holder = new Holder(1, "This is a value for id = " + id);
        log.info("❌ Missed cache for id = {}, loaded value: {}", id, holder);
    }
}
