package lsa.prototype.vem.model.context;

import jakarta.persistence.*;
import lsa.prototype.vem.model.basic.PersistedObject;
import lsa.prototype.vem.model.version.Leaf;

@MappedSuperclass
public class ChangeUnit<R extends ChangeRequest<?>> extends PersistedObject {
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "leaf_type")),
            @AttributeOverride(name = "id", column = @Column(name = "leaf_id"))
    })
    @Embedded
    private PolymorphEntity leaf;
    @ManyToOne
    private R request;
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

    public R getRequest() {
        return request;
    }

    public void setRequest(R request) {
        this.request = request;
    }
}
