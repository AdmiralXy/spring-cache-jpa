package io.github.admiralxy.cache.jpa.api.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.Instant;

@Converter(autoApply = true)
public class InstantTimestampConverter implements AttributeConverter<Instant, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(Instant inst) {
        return inst == null ? null : Timestamp.from(inst);
    }

    @Override
    public Instant convertToEntityAttribute(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}

