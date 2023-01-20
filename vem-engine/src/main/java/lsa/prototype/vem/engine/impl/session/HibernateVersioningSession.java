package lsa.prototype.vem.engine.impl.session;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lsa.prototype.vem.engine.impl.request.ChangerImpl;
import lsa.prototype.vem.model.basic.Particle;
import lsa.prototype.vem.model.context.ChangeOperation;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeState;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.EntityVersion;
import lsa.prototype.vem.model.version.LeafEntity;
import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.model.version.VersionedEntity;
import lsa.prototype.vem.spi.VersioningException;
import lsa.prototype.vem.spi.request.ChangeRequestSpecification;
import lsa.prototype.vem.spi.request.Changer;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;
import lsa.prototype.vem.spi.session.PersistenceProcessor;
import lsa.prototype.vem.spi.session.VersioningEntityManager;
import lsa.prototype.vem.spi.session.VersioningEntityManagerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        ChangeRequest<T> request = getChanger().createChangeRequest(entity);

        em.persist(request);
        em.persist(entity);

        processors.get("entity-persist").process(entity, entity, request, this);

        return request;
    }

    @Override
    public <T extends RootEntity> ChangeRequest<T> persist(ChangeRequestSpecification<T> specification) {
        if (specification == null || specification.getRoot() == null)
            return null;
        T entity = specification.getRoot();
        em.persist(entity);

        ChangeRequest<T> request = getChanger().createChangeRequest(entity);
        em.persist(request);

        return persistCRS(specification, entity, request);
    }

    @Override
    public <T extends RootEntity> ChangeRequest<T> merge(T entity) {
        if (entity == null || entity.getId() == 0)
            return null;
        T storedEntity = find((Class<T>) entity.getClass(), entity.getUuid());
        ChangeRequest<T> request = getChanger().createChangeRequest(storedEntity);

        em.persist(request);

        processors.get("entity-merge").process(request.getRoot(), entity, request, this);
        return request;
    }

    @Override
    public <T extends RootEntity> ChangeRequest<T> merge(ChangeRequestSpecification<T> specification) {
        T entity = specification.getRoot();
        if (entity == null || entity.getId() == 0)
            return null;

        ChangeRequest<T> request;
        if (specification.getUuid() == null) {
            request = getChanger().createChangeRequest(entity);
            em.persist(request);
        } else {
            Class<ChangeRequest<T>> type = getChanger().getRequestDatatype(entity).getJavaType();
            request = find(type, specification.getUuid());
        }
        if (request == null)
            return null;//todo

        T storedEntity = find((Class<T>) entity.getClass(), entity.getUuid());
        return persistCRS(specification, storedEntity, request);
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

        getChanger().stream(request, true).forEach(entity -> {
            processHistoryIncrement(entity, versionDate);
            em.persist(entity);
        });

        getChanger().stream(request, true).forEach(entity -> {
            processAffinityWiring(versionDate, entity);
        });

        request.setState(ChangeState.StateType.AFFIRMED, versionDate);
        em.persist(request);
    }

    @Override
    public <T extends RootEntity> void reject(ChangeRequest<T> request) {
        checkState(request, "reject", ChangeState.StateType.PUBLISHED);
        request.setState(ChangeState.StateType.REJECTED, System.currentTimeMillis());
        em.persist(request);
    }

    @Override
    public <T extends RootEntity> void destroy(ChangeRequest<T> request) {

    }

    @Override
    public <T extends RootEntity> void destroy(ChangeRequestSpecification<T> specification) {

    }

    @Override
    public <T extends Particle> T find(Class<T> type, UUID uuid) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(type);
        Root<T> root = query.from(type);
        query.select(root).where(cb.equal(root.get("uuid"), uuid));
        List<T> result = em.createQuery(query).getResultList();

        return switch (result.size()) {
            case 0 -> null;
            case 1 -> result.get(0);
            default -> throw new VersioningException("too many rows " + type.getName() + "(" + uuid + ")");
        };
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

    private <T extends RootEntity> ChangeRequest<T> persistCRS(ChangeRequestSpecification<T> specification, T entity, ChangeRequest<T> request) {
        specification.getUnits().forEach(u -> {
            em.persist(u.getLeaf());

            if (u.getOperation().equals(ChangeOperation.REMOVE))
                u.getLeaf().getVersion().setStateType(EntityVersion.StateType.PURGE);

            ChangeUnit<ChangeRequest<T>> unit = getChanger().createChangeUnit(
                    request,
                    u.getLeaf(),
                    u.getOperation()
            );
            em.persist(unit);
        });
        return request;
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
                if (entity instanceof LeafEntity<?>) {
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
}