package lsa.prototype.vem.model.context;

import jakarta.persistence.MappedSuperclass;
import lsa.prototype.vem.model.basic.PersistedObject;
import lsa.prototype.vem.model.version.Leaf;
import lsa.prototype.vem.model.version.Root;

@MappedSuperclass
abstract public class ChangeUnit<T extends Root> extends PersistedObject {
    abstract public Leaf<?> getLeaf();

    abstract public void setLeaf(Leaf<?> leaf);

    abstract public ChangeRequest<T> getRequest();

    abstract public void setRequest(ChangeRequest<T> request);

    private long date;

}
