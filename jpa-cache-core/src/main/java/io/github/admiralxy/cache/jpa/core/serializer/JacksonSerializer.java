package io.github.admiralxy.cache.jpa.core.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.admiralxy.cache.jpa.api.CacheException;
import io.github.admiralxy.cache.jpa.api.CacheSerializer;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JacksonSerializer implements CacheSerializer {

    private final ObjectMapper mapper;

    public byte[] serialize(@Nonnull Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    public <T> T deserialize(byte[] blob, @Nonnull Class<T> type) {
        try {
            return mapper.readValue(blob, type);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }
}
