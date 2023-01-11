package lsa.prototype.vem.engine.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lsa.prototype.vem.engine.spi.*;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.version.Root;
import lsa.prototype.vem.model.version.EntityVersion;
import lsa.prototype.vem.model.version.VersionedEntity;

import java.util.HashMap;
import java.util.Map;

public class HibernateVersioningSession implements VersioningEntityManager {
    private final VersioningEntityManagerFactory factory;
    private final EntityManager em;
    private final Map<String, PersistenceProcessor> processors;
    private final Changer changer;

    public HibernateVersioningSession(VersioningEntityManagerFactory factory, EntityManager em, Map<String, PersistenceProcessor> processors) {
        this.factory = factory;
        this.em = em;
        this.processors = new HashMap<>(processors);
        changer = new ChangerImpl(this);
    }

    @Override
    public <T extends Root, R extends ChangeRequest<T>> R persist(T entity) {
        if (entity == null)
            return null;

        R request = getChanger().instantiate(entity);

        em.persist(request);
        em.persist(entity);

        processors.get("recursive-persist").process(entity, entity, request, this);

        return request;
    }

    @Override
    public <T extends Root, R extends ChangeRequest<T>> R merge(T entity) {
        if (entity == null || entity.getId() == 0)
            return null;

        T storedEntity = em.find((Class<T>) entity.getClass(), entity.getId());
        R request = getChanger().instantiate(storedEntity);

        em.persist(request);

        processors.get("recursive-merge").process(storedEntity, entity, request, this);

        return request;
    }

    @Override
    public <T extends Root, R extends ChangeRequest<T>> R remove(T entity) {
        //todo
        return null;
    }

    @Override
    public <T extends Root, R extends ChangeRequest<T>> void affirm(R request) {
        if (!ChangeRequest.State.DRAFT.equals(request.getState()))
            throw new VersioningException("Ошибка при попытке подтвердить заявку на изменение в статусе " + request.getState());

        CriteriaBuilder cb = em.getCriteriaBuilder();
        long versionDate = System.currentTimeMillis();

        getChanger().fetchLeaves(request).forEach((type, entities) -> {
            for (VersionedEntity entity : entities) {

                CriteriaQuery<Object> query = cb.createQuery();
                jakarta.persistence.criteria.Root<?> root = query.from(type);

                query.select(root).where(
                        cb.equal(root.get("uuid"), entity.getUuid()),
                        cb.equal(root.get("versionState"), EntityVersion.State.ACTIVE)
                );

                em.createQuery(query)
                        .getResultList()
                        .stream()
                        .map(o -> (VersionedEntity) o)
                        .forEach(o -> {
                            o.getVersion().setState(EntityVersion.State.HISTORY);
                            em.persist(o);
                        });

                switch (entity.getVersion().getState()) {
                    case DRAFT -> entity.setVersion(new EntityVersion(EntityVersion.State.ACTIVE, versionDate));
                    case PURGE -> entity.setVersion(new EntityVersion(EntityVersion.State.PASSIVE, versionDate));
                }
                em.persist(entity);
            }
        });
    }

    @Override
    public <T extends Root, R extends ChangeRequest<T>> void reject(R request) {

    }

    @Override
    public EntityManager em() {
        return em;
    }

    @Override
    public VersioningEntityManagerFactory getFactory() {
        return factory;
    }

    @Override
    public void close() {
        em.close();
    }

    @Override
    public Changer getChanger() {
        return changer;
    }
}