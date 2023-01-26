package io.persistence.vem.spi.schema;

public interface Accessor {
    void set(Object entity, Object value);

    Object get(Object entity);
}
