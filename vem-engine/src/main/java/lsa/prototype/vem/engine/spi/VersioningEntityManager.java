package lsa.prototype.vem.engine.spi;

import jakarta.persistence.EntityManager;
import lsa.prototype.vem.engine.spi.schema.HistoryMappings;
import lsa.prototype.vem.engine.spi.schema.Schema;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.version.Root;

public interface VersioningEntityManager extends AutoCloseable {
    <T extends Root> ChangeRequest<T> persist(T entity);

    <T extends Root> ChangeRequest<T> merge(T entity);

    <T extends Root> ChangeRequest<T> remove(T entity);

    <T extends Root> void affirm(ChangeRequest<T> request);

    <T extends Root> void reject(ChangeRequest<T> request);

    EntityManager em();

    default Schema getSchema() {
        return getFactory().getSchema();
    }

    default HistoryMappings getHistoryMappings() {
        return getFactory().getHistoryMapping();
    }

    Changer getChanger();

    VersioningEntityManagerFactory getFactory();

    @Override
    void close();
}