package io.persistence.vem.spi.schema;

import java.util.Collection;

public interface PluralParameter<T> extends Parameter<T> {
    default <U> Collection<U> get(T owner) {
        return (Collection<U>) getAccessor().get(owner);
    }
}
