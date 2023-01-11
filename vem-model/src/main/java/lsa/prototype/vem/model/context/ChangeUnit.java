package lsa.prototype.vem.model.context;

import jakarta.persistence.*;
import lsa.prototype.vem.model.basic.PersistedObject;
import lsa.prototype.vem.model.version.Leaf;
import lsa.prototype.vem.model.version.Root;

@MappedSuperclass
abstract public class ChangeUnit<T extends Root> extends PersistedObject {
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "leaf_type")),
            @AttributeOverride(name = "id", column = @Column(name = "leaf_id"))
    })
    @Embedded
    private PolymorphEntity leaf;
    private long date;

    public PolymorphEntity getLeaf() {
        return leaf;
    }

    public void setLeaf(PolymorphEntity leaf) {
        this.leaf = leaf;
    }

    public void setLeaf(Leaf<?> leaf) {
        this.leaf = new PolymorphEntity(leaf);
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    abstract public ChangeRequest<T> getRequest();

    abstract public void setRequest(ChangeRequest<T> request);
}
