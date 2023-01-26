package io.persistence.vem.spi.function;

import io.persistence.vem.domain.model.Persistable;
import io.persistence.vem.spi.session.VersioningEntityManager;

public interface VisitorContext {
    VersioningEntityManager vem();

    <T, U> Axis<U> getAxis(T entity);

    boolean isVisited(Persistable entity);
}
