package lsa.prototype.vem.engine.spi.schema;

import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.Root;

public class HistoryMapping<T extends Root> {
    private final Datatype<T> root;
    private final Datatype<?> request;
    private final Datatype<?> unit;

    public HistoryMapping(Datatype<T> root, Datatype<?> request, Datatype<?> unit) {
        this.root = root;
        this.request = request;
        this.unit = unit;
    }

    public Datatype<T> root() {
        return root;
    }

    public <C extends ChangeRequest<T>> Datatype<C> request() {
        return (Datatype<C>) request;
    }

    public <U extends ChangeUnit<ChangeRequest<T>>> Datatype<U> unit() {
        return (Datatype<U>) unit;
    }
}
