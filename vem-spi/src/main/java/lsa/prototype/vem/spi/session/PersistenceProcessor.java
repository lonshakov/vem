package lsa.prototype.vem.spi.session;


import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.model.Versionable;
import lsa.prototype.vem.request.ChangeRequest;

@FunctionalInterface
public interface PersistenceProcessor {
    <T extends Root, R extends ChangeRequest<T>, V extends Versionable> void process(V oldEntity, V newEntity, R request, VersioningEntityManager vem);
}