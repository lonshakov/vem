package lsa.prototype.vem.engine.impl.request;

import lsa.prototype.vem.model.context.ChangeOperation;
import lsa.prototype.vem.model.version.LeafEntity;
import lsa.prototype.vem.spi.request.ChangeRequestSpecification;

public class CRUnitDTO implements ChangeRequestSpecification.Unit {
    private ChangeOperation operation;
    private LeafEntity<?> leaf;

    public CRUnitDTO(ChangeOperation operation, LeafEntity<?> leaf) {
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
    public LeafEntity<?> getLeaf() {
        return leaf;
    }
}
