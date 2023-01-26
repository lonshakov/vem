package io.persistence.vem.engine.impl.crs;

import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.engine.impl.function.Util;
import io.persistence.vem.spi.request.ChangeRequestSpecification;
import io.persistence.vem.spi.request.ChangeRequestSpecificationBuilder;
import io.persistence.vem.spi.session.VersioningEntityManager;

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