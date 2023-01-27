package io.persistence.vem.spi.schema;

public interface Schema extends Iterable<Datatype<?>> {
    <T> Datatype<T> getDatatype(Class<T> type);

    default <T> Datatype<T> getDatatype(T object) {
        return getDatatype((Class<T>) object.getClass());
    }
}
