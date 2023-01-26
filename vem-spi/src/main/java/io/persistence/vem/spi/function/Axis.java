package io.persistence.vem.spi.function;

import io.persistence.vem.spi.schema.Parameter;

public class Axis<T> {
    private final T parent;
    private final Parameter<T> parameter;

    public Axis(T parent, Parameter<T> parameter) {
        this.parent = parent;
        this.parameter = parameter;
    }

    public T getParent() {
        return parent;
    }

    public Parameter<T> getParameter() {
        return parameter;
    }
}
