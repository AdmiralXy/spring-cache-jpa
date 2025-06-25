package io.github.admiralxy.cache.jpa.core.eviction.impl;

import io.github.admiralxy.cache.jpa.core.eviction.EvictionSelector;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Implements a First-In-First-Out (FIFO) eviction policy.
 * <p>
 * When the cache exceeds its capacity, this selector chooses the oldest entries
 * (based on creation timestamp) for eviction first.
 * </p>
 *
 * @see EvictionSelector
 */
@RequiredArgsConstructor
public class FifoSelector extends EvictionSelector {

    private static final String JPQL_SELECT_VICTIMS =
            "SELECT e.id.key " +
                    "FROM JpaCacheEntity e " +
                    "WHERE e.id.name = :cacheName " +
                    "ORDER BY e.createdAt ASC";

    private final EntityManager em;

    @Override
    public List<String> selectVictims(String cacheName, int overflow) {
        TypedQuery<String> q = em.createQuery(JPQL_SELECT_VICTIMS, String.class);
        q.setParameter("cacheName", cacheName);
        q.setMaxResults(overflow);
        return q.getResultList();
    }
}
