package lsa.prototype.vem.spi.session;


import lsa.prototype.vem.model.IRootEntity;
import lsa.prototype.vem.model.IVersionedEntity;
import lsa.prototype.vem.request.IChangeRequest;

@FunctionalInterface
public interface PersistenceProcessor {
    <T extends IRootEntity, R extends IChangeRequest<T>, V extends IVersionedEntity> void process(V oldEntity, V newEntity, R request, VersioningEntityManager vem);
}