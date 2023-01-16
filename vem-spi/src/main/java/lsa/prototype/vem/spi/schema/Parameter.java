package lsa.prototype.vem.spi.schema;

import lsa.prototype.vem.model.basic.PersistedObject;

import java.io.Serializable;

public interface Parameter<T extends PersistedObject> {
    String getName();

    Class<?> getType();

    Datatype<?> getParameterDatatype();

    Datatype<T> getStructureDatatype();

    boolean isCollection();

    boolean isReference();

    default boolean isPrimitive() {
        return !isReference() && !isCollection();
    }

    void set(T owner, Serializable value);

    Object get(T owner);
}
