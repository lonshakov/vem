package io.persistence.vem.spi.schema;

import java.util.Map;

public interface Datatype<T> {
    T instantiate();

    T clone(T entity);

    Parameter<T> getIdentifier();

    Parameter<T> getGlobalIdentifier();

    Parameter<T> getPrimitive(String name);

    Parameter<T> getReference(String name);

    Parameter<T> getCollection(String name);

    Map<String, Parameter<T>> getPrimitives();

    Map<String, Parameter<T>> getReferences();

    Map<String, Parameter<T>> getCollections();

    default boolean isGlobal() {
        return getGlobalIdentifier() != null;
    }

    Class<T> getJavaType();

    Schema getSchema();
}
