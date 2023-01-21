package lsa.prototype.vem.engine.impl.request;

import lsa.prototype.vem.model.Leaf;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.request.ChangeRequestSpecification;

public class CRUnitDTO implements ChangeRequestSpecification.Unit {
    private ChangeOperation operation;
    private Leaf<?> leaf;

    public CRUnitDTO(ChangeOperation operation, Leaf<?> leaf) {
        this.operation = operation;
        this.leaf = leaf;
    }

    public CRUnitDTO() {
    }

    @Override
    public ChangeOperation getOperation() {
        return operation;
    }

    @Override
    public Leaf<?> getLeaf() {
        return leaf;
    }
}
