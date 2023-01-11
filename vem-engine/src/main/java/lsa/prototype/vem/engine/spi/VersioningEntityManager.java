package lsa.prototype.vem.engine.spi;

import jakarta.persistence.EntityManager;
import lsa.prototype.vem.engine.spi.schema.HistoryMappings;
import lsa.prototype.vem.engine.spi.schema.Schema;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.version.Root;

public interface VersioningEntityManager extends AutoCloseable {
    <T extends Root, R extends ChangeRequest<T>>
    R persist(T entity);

    <T extends Root, R extends ChangeRequest<T>>
    R merge(T entity);

    <T extends Root, R extends ChangeRequest<T>>
    R remove(T entity);

    <T extends Root, R extends ChangeRequest<T>>
    void affirm(R request);

    <T extends Root, R extends ChangeRequest<T>>
    void reject(R request);

    EntityManager em();

    default Schema getSchema() {
        return getFactory().getSchema();
    }

    default HistoryMappings getHistoryMapping() {
        return getFactory().getHistoryMapping();
    }

    Changer getChanger();

    VersioningEntityManagerFactory getFactory();

    @Override
    void close();
}