package lsa.prototype.vem.engine.spi;

import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.Root;
import lsa.prototype.vem.model.version.VersionedEntity;

import java.util.List;
import java.util.Map;

public interface Changer {
    <T extends Root, R extends ChangeRequest<T>> R instantiate(T entity);

    <T extends Root> Map<Class<?>, List<ChangeUnit<T>>> fetchUnits(ChangeRequest<T> request);

    <T extends Root> Map<Class<?>, List<VersionedEntity>> fetchLeaves(ChangeRequest<T> request);
}
