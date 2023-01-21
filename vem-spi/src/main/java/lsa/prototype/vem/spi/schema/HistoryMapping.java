package lsa.prototype.vem.spi.schema;

import lsa.prototype.vem.model.IRootEntity;
import lsa.prototype.vem.request.IChangeRequest;
import lsa.prototype.vem.request.IChangeUnit;

public class HistoryMapping<T extends IRootEntity> {
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

    public <C extends IChangeRequest<T>> Datatype<C> getRequestDatatype() {
        return (Datatype<C>) request;
    }

    public <U extends IChangeUnit<IChangeRequest<T>>> Datatype<U> getUnitDatatype() {
        return (Datatype<U>) unit;
    }
}
