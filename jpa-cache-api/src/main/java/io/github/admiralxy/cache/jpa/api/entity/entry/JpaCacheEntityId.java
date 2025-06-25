package io.github.admiralxy.cache.jpa.api.entity.entry;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Represents the composite key for a cache entry.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JpaCacheEntityId implements Serializable {

    /**
     * Cache name.
     */
    @Column(name = "c_name", nullable = false)
    private String name;

    /**
     * Cache key.
     */
    @Column(name = "c_key", nullable = false)
    private String key;
}
