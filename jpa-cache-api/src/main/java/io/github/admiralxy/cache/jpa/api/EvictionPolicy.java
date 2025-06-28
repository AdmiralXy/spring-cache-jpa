package io.github.admiralxy.cache.jpa.api;

/**
 * Built-in eviction algorithms used when a cache region exceeds {@code maxSize}.
 */
public enum EvictionPolicy {

    /** Least-recently used. */
    LRU,

    /** Least-frequently used. */
    LFU,

    /** First-in, first-out. */
    FIFO
}
