package vem.basemodel.request;

import jakarta.persistence.*;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.request.ChangeUnit;
import lsa.prototype.vem.request.PolymorphEntity;
import vem.basemodel.basic.PersistedObject;
import vem.basemodel.version.LeafEntity;

@MappedSuperclass
public class ChangeUnitTemplate<R extends ChangeRequestTemplate<?>> extends PersistedObject implements ChangeUnit<R> {
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "leaf_type")),
            @AttributeOverride(name = "id", column = @Column(name = "leaf_id"))
    })
    @Embedded
    private PolymorphEntity leaf;
    @ManyToOne
    private R request;
    private long date;
    private ChangeOperation operation;

    public PolymorphEntity getLeaf() {
        return leaf;
    }

    public void setLeaf(PolymorphEntity leaf) {
        this.leaf = leaf;
    }

    public void setLeaf(LeafEntity<?> leaf) {
        this.leaf = new PolymorphEntity(leaf.getClass(), leaf.getId());
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

    public ChangeOperation getOperation() {
        return operation;
    }

    public void setOperation(ChangeOperation operation) {
        this.operation = operation;
    }
}
