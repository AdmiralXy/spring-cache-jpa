package io.github.admiralxy.cache.jpa.core.eviction;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Selects keys that have to be removed in order to bring the region size under the configured {@code maxSize}.
 */
@RequiredArgsConstructor
public abstract class EvictionSelector {

    /**
     * Selects keys that must be removed from the cache region to bring its size.
     *
     * @param cacheName region to inspect
     * @param overflow  number of keys that must be removed
     * @return list containing {@code overflow} keys
     */
    public abstract List<String> selectVictims(String cacheName, int overflow);
}
