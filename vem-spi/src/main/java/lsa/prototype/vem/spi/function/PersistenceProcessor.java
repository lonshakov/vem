package lsa.prototype.vem.spi.function;


import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.model.Versionable;
import lsa.prototype.vem.request.ChangeRequest;
import lsa.prototype.vem.spi.session.VersioningEntityManager;

@FunctionalInterface
public interface PersistenceProcessor {
    <T extends Root, R extends ChangeRequest<T>, V extends Versionable> void process(V entity, R request, VersioningEntityManager vem);
}