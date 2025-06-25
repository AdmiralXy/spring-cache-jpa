package io.github.admiralxy.cache.jpa.api.entity.entry;

import io.github.admiralxy.cache.jpa.api.entity.converter.InstantTimestampConverter;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents a cache entry.
 */
@Entity
@Table(name = "t_jpa_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JpaCacheEntity {

    /**
     * The primary key for the cache entry, consisting of the cache name and the key.
     */
    @EmbeddedId
    private JpaCacheEntityId id;

    /**
     * The serialized payload of the cache entry.
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "c_payload", nullable = false)
    private byte[] payload;

    /**
     * Creation time.
     */
    @Column(name = "c_created_at", nullable = false)
    @Convert(converter = InstantTimestampConverter.class)
    private Instant createdAt;

    /**
     * Expiration time.
     */
    @Column(name = "c_expires_at", nullable = false)
    @Convert(converter = InstantTimestampConverter.class)
    private Instant expiresAt;

    /**
     * Last accessed time.
     */
    @Column(name = "c_last_accessed_at", nullable = false)
    @Convert(converter = InstantTimestampConverter.class)
    private Instant lastAccessedAt;

    /**
     * Hit count, indicating how many times this entry has been accessed.
     */
    @Column(name = "c_hit_count", nullable = false)
    private long hitCount;
}

