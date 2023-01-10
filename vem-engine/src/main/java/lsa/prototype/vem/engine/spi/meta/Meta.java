package lsa.prototype.vem.engine.spi.meta;

import lsa.prototype.vem.model.basic.PersistedObject;

public interface Meta extends Iterable<Datatype<?>> {
    <T extends PersistedObject> Datatype<T> datatype(Class<T> type);

    default <T extends PersistedObject> Datatype<T> datatype(T object) {
        return datatype((Class<T>) object.getClass());
    }
}
