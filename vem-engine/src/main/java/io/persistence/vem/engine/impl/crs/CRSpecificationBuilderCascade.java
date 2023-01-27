package io.persistence.vem.engine.impl.crs;

import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.engine.impl.function.Util;
import io.persistence.vem.spi.request.ChangeRequestSpecification;
import io.persistence.vem.spi.request.ChangeRequestSpecificationBuilder;
import io.persistence.vem.spi.session.VersioningEntityManager;

import java.io.Serializable;

public class CRSpecificationBuilderCascade<T extends Root> implements ChangeRequestSpecificationBuilder<T> {
    private final ChangeOperation operation;
    private final VersioningEntityManager vem;

    public CRSpecificationBuilderCascade(ChangeOperation operation, VersioningEntityManager vem) {
        this.vem = vem;
        this.operation = operation;
    }

    @Override
    public ChangeRequestSpecification<T> build(Serializable uuid, T root) {
        ChangeRequestSpecification<T> specification = new CRSpecificationDTO<>(uuid, root);
        Util.defineChangeOperationCascade(root, vem, specification, operation);
        return specification;
    }
}