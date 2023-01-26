package io.persistence.vem.spi.request;

import io.persistence.vem.domain.model.Root;
import io.persistence.vem.spi.session.VersioningEntityManager;

public interface ChangeRequestSpecificationBuilder {
    <T extends Root> ChangeRequestSpecification<T> build(T root, VersioningEntityManager vem);
}
