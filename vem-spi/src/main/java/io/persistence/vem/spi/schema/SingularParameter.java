package io.persistence.vem.spi.schema;

public interface SingularParameter<T> extends Parameter<T> {
    default <U> U get(T owner) {
        return (U) getAccessor().get(owner);
    }
}
