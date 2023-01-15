package lsa.prototype.vem.spi.schema;

import lsa.prototype.vem.model.basic.PersistedObject;

import java.util.Map;

public interface Datatype<T extends PersistedObject> {
    T instantiate();

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
