package io.github.admiralxy.cache.jpa.api;

import jakarta.annotation.Nonnull;

/**
 * Strategy for (de)serialising cached values to a binary form that can be persisted in the database.
 */
public interface CacheSerializer {

    /**
     * Serialises the given value to bytes.
     *
     * @param value value
     * @return byte array
     */
    byte[] serialize(@Nonnull Object value);

    /**
     * Restores an object from its binary representation.
     *
     * @param blob payload
     * @param type target type
     * @param <T>  return type
     * @return deserialized object of type {@code T}
     */
    <T> T deserialize(byte[] blob, @Nonnull Class<T> type);
}
