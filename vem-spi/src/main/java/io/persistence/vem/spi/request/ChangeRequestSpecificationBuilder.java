package io.persistence.vem.spi.request;

import io.persistence.vem.domain.model.Root;

import java.io.Serializable;

public interface ChangeRequestSpecificationBuilder<T extends Root> {
    ChangeRequestSpecification<T> build(Serializable uuid, T root);
}
