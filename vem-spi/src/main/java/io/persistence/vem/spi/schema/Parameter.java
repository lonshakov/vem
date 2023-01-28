package io.persistence.vem.spi.schema;

import javax.persistence.metamodel.Attribute;

public interface Parameter<T> {
    default String getName() {
        return getAttribute().getName();
    }

    Class<?> getJavaType();

    Class<?> getGraphType();

    Attribute<T, ?> getAttribute();

    Datatype<?> getParameterDatatype();

    Datatype<T> getStructureDatatype();

    default boolean isCollection() {
        return !isPrimitive() && getAttribute().isCollection();
    }

    default boolean isReference() {
        return !isPrimitive() && !getAttribute().isCollection();
    }

    default boolean isPrimitive() {
        return !getAttribute().isAssociation();
    }

    default boolean isVersionable() {
        return getParameterDatatype().isVersionable();
    }

    Accessor getAccessor();

    default void set(T owner, Object value) {
        getAccessor().set(owner, value);
    }
}
