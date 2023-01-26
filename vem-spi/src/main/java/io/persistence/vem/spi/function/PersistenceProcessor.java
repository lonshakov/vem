package io.persistence.vem.spi.function;


import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.model.Versionable;
import io.persistence.vem.domain.request.ChangeRequest;
import io.persistence.vem.spi.session.VersioningEntityManager;

@FunctionalInterface
public interface PersistenceProcessor {
    <T extends Root, R extends ChangeRequest<T>, V extends Versionable> void process(V entity, R request, VersioningEntityManager vem);
}