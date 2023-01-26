package io.persistence.vem.spi.session;

import io.persistence.vem.domain.model.GlobalEntity;
import io.persistence.vem.domain.model.Persistable;
import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeRequest;
import io.persistence.vem.spi.function.VisitorContext;
import io.persistence.vem.spi.request.ChangeRequestSpecification;
import io.persistence.vem.spi.request.Changer;
import io.persistence.vem.spi.schema.HistoryMappings;
import io.persistence.vem.spi.schema.Schema;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.function.BiConsumer;

public interface VersioningEntityManager extends AutoCloseable {
    <T extends Root> ChangeRequest<T> persist(T entity);

    <T extends Root> ChangeRequest<T> persist(ChangeRequestSpecification<T> specification);

    <T extends Root> ChangeRequest<T> merge(T entity);

    <T extends Root> ChangeRequest<T> merge(ChangeRequestSpecification<T> specification);

    <T extends Root> ChangeRequest<T> remove(T entity);

    <T extends Root> void publish(ChangeRequest<T> request);

    <T extends Root> void affirm(ChangeRequest<T> request);

    <T extends Root> void reject(ChangeRequest<T> request);

    <T extends Root> void destroy(ChangeRequest<T> request);

    <T extends Root> void destroy(ChangeRequestSpecification<T> specification);

    <T extends GlobalEntity> T find(Class<T> type, Serializable uuid);

    <T extends Persistable> void cascade(T entity, BiConsumer<Persistable, VisitorContext> task);

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