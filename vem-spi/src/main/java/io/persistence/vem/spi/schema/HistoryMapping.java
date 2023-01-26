package io.persistence.vem.spi.schema;

import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeRequest;
import io.persistence.vem.domain.request.ChangeUnit;

public class HistoryMapping<T extends Root> {
    private final Datatype<T> root;
    private final Datatype<?> request;
    private final Datatype<?> unit;

    public HistoryMapping(Datatype<T> root, Datatype<?> request, Datatype<?> unit) {
        this.root = root;
        this.request = request;
        this.unit = unit;
    }

    public Datatype<T> getRootDatatype() {
        return root;
    }

    public <C extends ChangeRequest<T>> Datatype<C> getRequestDatatype() {
        return (Datatype<C>) request;
    }

    public <U extends ChangeUnit<ChangeRequest<T>>> Datatype<U> getUnitDatatype() {
        return (Datatype<U>) unit;
    }
}
