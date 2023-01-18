package lsa.prototype.vem.engine.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeState;
import lsa.prototype.vem.model.version.EntityVersion;
import lsa.prototype.vem.model.version.LeafEntity;
import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.model.version.VersionedEntity;
import lsa.prototype.vem.spi.*;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;

import java.util.HashMap;
import java.util.List;
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
    public <T extends RootEntity> ChangeRequest<T> persist(T entity) {
        if (entity == null)
            return null;

        ChangeRequest<T> request = getChanger().instantiate(entity);

        em.persist(request);
        em.persist(entity);

        processors.get("recursive-persist").process(entity, entity, request, this);

        return request;
    }

    @Override
    public <T extends RootEntity> ChangeRequest<T> merge(T entity) {
        if (entity == null || entity.getId() == 0)
            return null;

        T storedEntity = em.find((Class<T>) entity.getClass(), entity.getId());
        ChangeRequest<T> request = getChanger().instantiate(storedEntity);

        em.persist(request);

        processors.get("recursive-merge").process(storedEntity, entity, request, this);

        return request;
    }

    @Override
    public <T extends RootEntity> ChangeRequest<T> remove(T entity) {
        //todo
        return null;
    }

    @Override
    public <T extends RootEntity> void publish(ChangeRequest<T> request) {
        checkState(request, "publish", ChangeState.StateType.DRAFT);
        request.getState().setStateType(ChangeState.StateType.PUBLISHED);
    }

    @Override
    public <T extends RootEntity> void affirm(ChangeRequest<T> request) {
        checkState(request, "affirm", ChangeState.StateType.PUBLISHED);

        long versionDate = System.currentTimeMillis();

        T root = request.getRoot();
        if (root.getVersion().getStateType().equals(EntityVersion.StateType.DRAFT)) {
            request.getRoot().setVersion(EntityVersion.StateType.ACTIVE, versionDate);
            em.persist(request.getRoot());
        }

        Map<Class<?>, List<LeafEntity<?>>> units = getChanger().map(request);

        units.forEach((type, entities) -> entities.forEach(entity -> {
            processHistoryIncrement(entity, versionDate);
            em.persist(entity);
        }));

        units.forEach((type, entities) -> entities.forEach(entity -> processAffinityWiring(versionDate, entity)));

        request.setState(ChangeState.StateType.AFFIRMED, versionDate);
        em.persist(request);
    }

    private void processAffinityWiring(long versionDate, VersionedEntity entity) {
        switch (entity.getVersion().getStateType()) {
            case ACTIVE -> {
                if (entity instanceof LeafEntity<?>) {
                    LeafEntity<VersionedEntity> orphan = (LeafEntity<VersionedEntity>) entity;
                    VersionedEntity parent = em
                            .createQuery(getActiveParentQuery(orphan))
                            .getSingleResult();
                    orphan.setParent(parent);
                }
            }
            case PASSIVE -> {
                if (entity instanceof LeafEntity<?>){
                    LeafEntity<VersionedEntity> nonOrphan = (LeafEntity<VersionedEntity>) entity;
                    nonOrphan.setParent(null);
                }
            }
        }
    }

    private <T extends VersionedEntity> void processHistoryIncrement(T entity, long versionDate) {
        for (T activeEntity : em.createQuery(getCurrentVersionQuery(entity)).getResultList()) {
            activeEntity.getVersion().setStateType(
                    EntityVersion.StateType.HISTORY
            );
            if (activeEntity instanceof LeafEntity<?>) {
                ((LeafEntity<?>) activeEntity).setParent(null);
            }
            em.persist(activeEntity);
        }
        switch (entity.getVersion().getStateType()) {
            case DRAFT -> entity.setVersion(EntityVersion.StateType.ACTIVE, versionDate);
            case PURGE -> entity.setVersion(EntityVersion.StateType.PASSIVE, versionDate);
        }
    }

    private <T extends VersionedEntity> CriteriaQuery<T> getCurrentVersionQuery(T entity) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<T> type = (Class<T>) entity.getClass();

        CriteriaQuery<T> query = cb.createQuery(type);
        Root<T> root = query.from(type);

        query.select(root).where(
                cb.equal(root.get("uuid"), entity.getUuid()),
                cb.equal(root.get("version").get("stateType"), EntityVersion.StateType.ACTIVE)
        );

        return query;
    }

    private <T extends LeafEntity<P>, P extends VersionedEntity> CriteriaQuery<P> getActiveParentQuery(T entity) {
        Datatype<T> datatype = getSchema().datatype(entity);
        Parameter<T> parent = datatype.reference("parent");

        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<P> type = (Class<P>) parent.getParameterDatatype().getJavaType();

        CriteriaQuery<P> query = cb.createQuery(type);
        Root<P> root = query.from(type);

        query.select(root).where(
                cb.equal(root.get("uuid"), entity.getAffinity()),
                cb.equal(root.get("version").get("stateType"), EntityVersion.StateType.ACTIVE)
        );

        return query;
    }

    @Override
    public <T extends RootEntity> void reject(ChangeRequest<T> request) {
        checkState(request, "reject", ChangeState.StateType.PUBLISHED);
        //todo
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

    private static <T extends RootEntity> void checkState(ChangeRequest<T> request, String methodName, ChangeState.StateType stateType) {
        if (!stateType.equals(request.getState().getStateType()))
            throw new VersioningException("Ошибка при попытке обработать методом " + methodName + " заявку на изменение в статусе " + request.getState());
    }
}