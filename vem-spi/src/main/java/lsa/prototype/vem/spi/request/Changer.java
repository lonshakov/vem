package lsa.prototype.vem.spi.request;

import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.version.LeafEntity;
import lsa.prototype.vem.model.version.RootEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface Changer {
    <T extends RootEntity> ChangeRequest<T> instantiate(T entity);

    <T extends RootEntity> Stream<LeafEntity<?>> stream(ChangeRequest<T> request);

    <T extends RootEntity> Map<Class<?>, List<LeafEntity<?>>> map(ChangeRequest<T> request);
}
