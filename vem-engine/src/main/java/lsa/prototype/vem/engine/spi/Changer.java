package lsa.prototype.vem.engine.spi;

import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.Root;
import lsa.prototype.vem.model.version.VersionedEntity;

import java.util.List;
import java.util.Map;

public interface Changer {
    <T extends Root, R extends ChangeRequest<T>> R instantiate(T entity);

    <R extends ChangeRequest<?>> Map<Class<?>, List<ChangeUnit<R>>> fetchUnits(R request);

    <R extends ChangeRequest<?>> Map<Class<?>, List<VersionedEntity>> fetchLeaves(R request);
}
