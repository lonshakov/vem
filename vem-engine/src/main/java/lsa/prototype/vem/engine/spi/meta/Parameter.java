package lsa.prototype.vem.engine.spi.meta;

import lsa.prototype.vem.model.basic.PersistedObject;

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

    void set(T owner, Object value);

     Object get(T owner);
}
