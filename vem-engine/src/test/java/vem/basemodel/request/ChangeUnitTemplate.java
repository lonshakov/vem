package vem.basemodel.request;

import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.domain.request.ChangeUnit;
import io.persistence.vem.domain.request.PolymorphEntity;
import vem.basemodel.basic.PersistedObject;
import vem.basemodel.version.LeafEntity;

import javax.persistence.*;

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
