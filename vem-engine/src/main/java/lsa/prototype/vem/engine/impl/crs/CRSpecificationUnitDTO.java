package lsa.prototype.vem.engine.impl.crs;

import lsa.prototype.vem.model.Leaf;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.spi.request.ChangeRequestSpecification;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CRSpecificationUnitDTO crSpecificationUnitDTO = (CRSpecificationUnitDTO) o;
        return leaf.getUuid().equals(crSpecificationUnitDTO.leaf.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(leaf.getUuid());
    }
}
