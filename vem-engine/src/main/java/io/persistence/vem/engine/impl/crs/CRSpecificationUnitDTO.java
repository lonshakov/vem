package io.persistence.vem.engine.impl.crs;

import io.persistence.vem.domain.model.Leaf;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.spi.request.ChangeRequestSpecification;

public class CRSpecificationUnitDTO implements ChangeRequestSpecification.Unit {
    private ChangeOperation operation;
    private Leaf<?> leaf;

    public CRSpecificationUnitDTO(ChangeOperation operation, Leaf<?> leaf) {
        this.operation = operation;
        this.leaf = leaf;

    }

    public CRSpecificationUnitDTO() {
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
