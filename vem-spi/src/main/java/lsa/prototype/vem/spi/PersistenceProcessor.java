package lsa.prototype.vem.spi;

import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.version.Root;
import lsa.prototype.vem.model.version.VersionedEntity;

@FunctionalInterface
public interface PersistenceProcessor {
    <T extends Root, R extends ChangeRequest<T>, V extends VersionedEntity> void process(V oldEntity, V newEntity, R request, VersioningEntityManager vem);
}