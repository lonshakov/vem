package lsa.prototype.vem.spi.function;

import lsa.prototype.vem.model.Persistable;
import lsa.prototype.vem.spi.session.VersioningEntityManager;

public interface VisitorContext {
    VersioningEntityManager vem();

    <T, U> Axis<U> getAxis(T entity);

    boolean isVisited(Persistable entity);
}
