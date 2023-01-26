package io.persistence.vem.spi.schema;

import java.util.Map;

public interface Datatype<T> {
    T instantiate();

    T clone(T entity);

    Parameter<T> identifier();

    Parameter<T> primitive(String name);

    Parameter<T> reference(String name);

    Parameter<T> collection(String name);

    Map<String, Parameter<T>> primitives();

    Map<String, Parameter<T>> references();

    Map<String, Parameter<T>> collections();

    Class<T> getJavaType();

    Schema getSchema();
}
