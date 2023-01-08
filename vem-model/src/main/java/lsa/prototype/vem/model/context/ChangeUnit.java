package lsa.prototype.vem.model.context;

import jakarta.persistence.MappedSuperclass;
import lsa.prototype.vem.model.basic.PersistedObject;
import lsa.prototype.vem.model.version.Leaf;

@MappedSuperclass
abstract public class ChangeUnit extends PersistedObject {
    abstract public Leaf<?> getLeaf();

    abstract public ChangeRequest<?> getChangeRequest();

    private long date;

}
