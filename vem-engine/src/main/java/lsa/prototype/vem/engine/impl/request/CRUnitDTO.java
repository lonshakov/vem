package lsa.prototype.vem.engine.impl.request;

import lsa.prototype.vem.model.ILeafEntity;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.request.IChangeRequestSpecification;

public class CRUnitDTO implements IChangeRequestSpecification.Unit {
    private ChangeOperation operation;
    private ILeafEntity<?> leaf;

    public CRUnitDTO(ChangeOperation operation, ILeafEntity<?> leaf) {
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
    public ILeafEntity<?> getLeaf() {
        return leaf;
    }
}
