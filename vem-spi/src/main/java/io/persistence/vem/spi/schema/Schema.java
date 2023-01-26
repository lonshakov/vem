package io.persistence.vem.spi.schema;

public interface Schema extends Iterable<Datatype<?>> {
    <T> Datatype<T> datatype(Class<T> type);

    default <T> Datatype<T> datatype(T object) {
        return datatype((Class<T>) object.getClass());
    }
}
