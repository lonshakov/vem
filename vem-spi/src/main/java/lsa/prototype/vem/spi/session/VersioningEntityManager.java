package lsa.prototype.vem.spi.session;

import jakarta.persistence.EntityManager;
import lsa.prototype.vem.model.basic.Particle;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.spi.request.ChangeRequestSpecification;
import lsa.prototype.vem.spi.request.Changer;
import lsa.prototype.vem.spi.schema.HistoryMappings;
import lsa.prototype.vem.spi.schema.Schema;

import java.util.UUID;

public interface VersioningEntityManager extends AutoCloseable {
    <T extends RootEntity> ChangeRequest<T> persist(T entity);

    <T extends RootEntity> ChangeRequest<T> persist(ChangeRequestSpecification<T> specification);

    <T extends RootEntity> ChangeRequest<T> merge(T entity);

    <T extends RootEntity> ChangeRequest<T> merge(ChangeRequestSpecification<T> specification);

    <T extends RootEntity> ChangeRequest<T> remove(T entity);

    <T extends RootEntity> void publish(ChangeRequest<T> request);

    <T extends RootEntity> void affirm(ChangeRequest<T> request);

    <T extends RootEntity> void reject(ChangeRequest<T> request);

    <T extends RootEntity> void destroy(ChangeRequest<T> request);

    <T extends RootEntity> void destroy(ChangeRequestSpecification<T> specification);

    <T extends Particle> T find(Class<T> type, UUID uuid);

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