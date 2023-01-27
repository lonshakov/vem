package io.persistence.vem.spi.function;

import io.persistence.vem.spi.schema.Schema;

import java.io.Serializable;
import java.util.Objects;

public interface PersistenceUtil {
    Schema getSchema();

    Serializable getId(Object obj);

    Serializable getUuid(Object obj);

    default boolean equals(Object obj1, Object obj2) {
        return Objects.equals(getUuid(obj1), getUuid(obj2)) || Objects.equals(obj1, obj2);
    }
}
