package lsa.prototype.vem.engine.impl.lab;

import lsa.prototype.vem.engine.impl.request.CRSpecificationDTO;
import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.spi.request.ChangeRequestSpecification;
import lsa.prototype.vem.spi.session.VersioningEntityManager;

public class CRSCascadeOperationBuilder implements ChangeRequestSpecificationBuilder {
    private final ChangeOperation operation;

    public CRSCascadeOperationBuilder(ChangeOperation operation) {
        this.operation = operation;
    }

    @Override
    public <T extends Root> ChangeRequestSpecification<T> build(T root, VersioningEntityManager vem) {
        ChangeRequestSpecification<T> specification = new CRSpecificationDTO<>(root);
        CRSUtil.defineChangeOperationCascade(root, vem, specification, operation);
        return specification;
    }
}