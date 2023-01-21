package lsa.prototype.vem.spi.session;

import jakarta.persistence.EntityManager;
import lsa.prototype.vem.model.IGlobalEntity;
import lsa.prototype.vem.model.IRootEntity;
import lsa.prototype.vem.request.IChangeRequest;
import lsa.prototype.vem.request.IChangeRequestSpecification;
import lsa.prototype.vem.spi.request.Changer;
import lsa.prototype.vem.spi.schema.HistoryMappings;
import lsa.prototype.vem.spi.schema.Schema;

import java.io.Serializable;

public interface VersioningEntityManager extends AutoCloseable {
    <T extends IRootEntity> IChangeRequest<T> persist(T entity);

    <T extends IRootEntity> IChangeRequest<T> persist(IChangeRequestSpecification<T> specification);

    <T extends IRootEntity> IChangeRequest<T> merge(T entity);

    <T extends IRootEntity> IChangeRequest<T> merge(IChangeRequestSpecification<T> specification);

    <T extends IRootEntity> IChangeRequest<T> remove(T entity);

    <T extends IRootEntity> void publish(IChangeRequest<T> request);

    <T extends IRootEntity> void affirm(IChangeRequest<T> request);

    <T extends IRootEntity> void reject(IChangeRequest<T> request);

    <T extends IRootEntity> void destroy(IChangeRequest<T> request);

    <T extends IRootEntity> void destroy(IChangeRequestSpecification<T> specification);

    <T extends IGlobalEntity> T find(Class<T> type, Serializable uuid);

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