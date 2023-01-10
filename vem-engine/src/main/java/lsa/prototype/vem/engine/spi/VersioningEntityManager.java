package lsa.prototype.vem.engine.spi;

import jakarta.persistence.EntityManager;
import lsa.prototype.vem.engine.spi.meta.HistoryMapping;
import lsa.prototype.vem.engine.spi.meta.Meta;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.version.Root;

public interface VersioningEntityManager extends AutoCloseable {
    <T extends Root, R extends ChangeRequest<T>> R persist(T entity);

    <T extends Root, R extends ChangeRequest<T>> R merge(T entity);

    <T extends Root, R extends ChangeRequest<T>> R remove(T entity);

    EntityManager em();

    default Meta meta() {
        return factory().meta();
    }

    default HistoryMapping getHistoryMapping() {
        return factory().getHistoryMapping();
    }

    VersioningEntityManagerFactory factory();

    @Override
    void close();
}
