package lsa.prototype.vem.engine.impl.crs;

import lsa.prototype.vem.engine.impl.function.Util;
import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.spi.request.ChangeRequestSpecification;
import lsa.prototype.vem.spi.request.ChangeRequestSpecificationBuilder;
import lsa.prototype.vem.spi.session.VersioningEntityManager;

public class CRSpecificationBuilderCascade implements ChangeRequestSpecificationBuilder {
    private final ChangeOperation operation;

    public CRSpecificationBuilderCascade(ChangeOperation operation) {
        this.operation = operation;
    }

    @Override
    public <T extends Root> ChangeRequestSpecification<T> build(T root, VersioningEntityManager vem) {
        ChangeRequestSpecification<T> specification = new CRSpecificationDTO<>(root);
        Util.defineChangeOperationCascade(root, vem, specification, operation);
        return specification;
    }
}