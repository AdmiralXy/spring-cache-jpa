package io.github.admiralxy.cache.jpa.core.eviction.impl;

import io.github.admiralxy.cache.jpa.core.eviction.EvictionSelector;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Implements a Least-Frequently-Used (LFU) eviction policy.
 * <p>
 * When the cache exceeds its capacity, this selector chooses the entries with the
 * lowest access frequency for eviction first. In case of ties on hit count, it
 * evicts the oldest among those with the same hit count.
 * </p>
 *
 * @see EvictionSelector
 */
@RequiredArgsConstructor
public class LfuSelector extends EvictionSelector {

    private static final String JPQL_SELECT_VICTIMS =
            "SELECT e.id.key " +
                    "FROM JpaCacheEntity e " +
                    "WHERE e.id.name = :cacheName " +
                    "ORDER BY e.hitCount ASC, e.createdAt ASC";

    private final EntityManager em;

    @Override
    public List<String> selectVictims(String cacheName, int overflow) {
        TypedQuery<String> q = em.createQuery(JPQL_SELECT_VICTIMS, String.class);
        q.setParameter("cacheName", cacheName);
        q.setMaxResults(overflow);
        return q.getResultList();
    }
}
