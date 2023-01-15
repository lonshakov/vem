package lsa.prototype.vem.engine.spi;

import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.version.Leaf;
import lsa.prototype.vem.model.version.Root;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface Changer {
    <T extends Root> ChangeRequest<T> instantiate(T entity);

    <T extends Root> Stream<Leaf<?>> stream(ChangeRequest<T> request);

    <T extends Root> Map<Class<?>, List<Leaf<?>>> map(ChangeRequest<T> request);
}
