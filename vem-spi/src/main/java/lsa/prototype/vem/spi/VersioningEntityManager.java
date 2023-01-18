package lsa.prototype.vem.spi;

import jakarta.persistence.EntityManager;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.spi.schema.HistoryMappings;
import lsa.prototype.vem.spi.schema.Schema;

public interface VersioningEntityManager extends AutoCloseable {
    <T extends RootEntity> ChangeRequest<T> persist(T entity);

    <T extends RootEntity> ChangeRequest<T> merge(T entity);

    <T extends RootEntity> ChangeRequest<T> remove(T entity);

    <T extends RootEntity> void publish(ChangeRequest<T> request);

    <T extends RootEntity> void affirm(ChangeRequest<T> request);

    <T extends RootEntity> void reject(ChangeRequest<T> request);

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