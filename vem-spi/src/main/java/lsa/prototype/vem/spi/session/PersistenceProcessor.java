package lsa.prototype.vem.spi.session;

import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.model.version.VersionedEntity;
import lsa.prototype.vem.spi.session.VersioningEntityManager;

@FunctionalInterface
public interface PersistenceProcessor {
    <T extends RootEntity, R extends ChangeRequest<T>, V extends VersionedEntity> void process(V oldEntity, V newEntity, R request, VersioningEntityManager vem);
}