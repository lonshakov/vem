package io.persistence.vem.spi.schema;

import io.persistence.vem.domain.model.Versionable;

import java.util.Map;

public interface Datatype<T> {
    T instantiate();

    T clone(T entity);

    SingularParameter<T> getIdentifier();

    SingularParameter<T> getGlobalIdentifier();

    SingularParameter<T> getPrimitive(String name);

    SingularParameter<T> getReference(String name);

    PluralParameter<T> getCollection(String name);

    Map<String, SingularParameter<T>> getPrimitives();

    Map<String, SingularParameter<T>> getReferences();

    Map<String, PluralParameter<T>> getCollections();

    default boolean isGlobal() {
        return getGlobalIdentifier() != null;
    }

    default boolean isVersionable() {
        return Versionable.class.isAssignableFrom(getJavaType());
    }

    Class<T> getJavaType();

    Schema getSchema();
}
