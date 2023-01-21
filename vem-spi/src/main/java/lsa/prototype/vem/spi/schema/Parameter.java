package lsa.prototype.vem.spi.schema;

public interface Parameter<T> {
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
