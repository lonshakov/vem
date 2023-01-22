package lsa.prototype.vem.engine.impl.session;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lsa.prototype.vem.engine.impl.request.ChangerImpl;
import lsa.prototype.vem.model.*;
import lsa.prototype.vem.request.*;
import lsa.prototype.vem.spi.VersioningException;
import lsa.prototype.vem.spi.request.Changer;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;
import lsa.prototype.vem.spi.session.PersistenceProcessor;
import lsa.prototype.vem.spi.session.VersioningEntityManager;
import lsa.prototype.vem.spi.session.VersioningEntityManagerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public <T extends Root> ChangeRequest<T> persist(T entity) {
        Objects.requireNonNull(entity);

        ChangeRequest<T> request = getChanger().createChangeRequest(entity);

        em.persist(request);
        em.persist(entity);

        processors.get("entity-persist").process(entity, entity, request, this);

        return request;
    }

    @Override
    public <T extends Root> ChangeRequest<T> persist(ChangeRequestSpecification<T> specification) {
        Objects.requireNonNull(specification);

        T entity = specification.getRoot();
        Objects.requireNonNull(entity);

        em.persist(entity);

        ChangeRequest<T> request = getChanger().createChangeRequest(entity);
        em.persist(request);

        return persistCRS(specification, entity, request);
    }

    @Override
    public <T extends Root> ChangeRequest<T> merge(T entity) {
        Objects.requireNonNull(entity);
        T storedEntity = findNonNull((Class<T>) entity.getClass(), entity.getUuid());

        ChangeRequest<T> request = getChanger().createChangeRequest(storedEntity);
        em.persist(request);

        processors.get("entity-merge").process(request.getRoot(), entity, request, this);
        return request;
    }

    @Override
    public <T extends Root> ChangeRequest<T> merge(ChangeRequestSpecification<T> specification) {
        Objects.requireNonNull(specification);
        T entity = findNonNull((Class<T>) specification.getRoot().getClass(), specification.getRoot().getUuid());

        ChangeRequest<T> request;
        if (specification.getUuid() == null) {
            request = getChanger().createChangeRequest(entity);
            em.persist(request);
        } else {
            Class<ChangeRequest<T>> type = getChanger().getRequestDatatype(entity).getJavaType();
            request = findNonNull(type, specification.getUuid());
        }
        return persistCRS(specification, entity, request);
    }

    @Override
    public <T extends Root> ChangeRequest<T> remove(T entity) {
        Objects.requireNonNull(entity);
        //todo
        return null;
    }

    @Override
    public <T extends Root> void publish(ChangeRequest<T> request) {
        checkRequestBeforeUpdate(request, "publish", ChangeState.DRAFT);
        getSchema().datatype(request).primitive("state").set(request, ChangeState.PUBLISHED);
    }

    @Override
    public <T extends Root> void affirm(ChangeRequest<T> request) {
        checkRequestBeforeUpdate(request, "affirm", ChangeState.PUBLISHED);
        long versionDate = System.currentTimeMillis();

        T root = request.getRoot();
        if (root.getVersion().getState().equals(VersionState.DRAFT)) {
            getSchema().datatype(root).primitive("version").set(root, new Version(VersionState.ACTIVE, 0));
            em.persist(request.getRoot());
        }

        getChanger().stream(request, true).forEach(entity -> {
            processHistoryIncrement(entity, versionDate);
            em.persist(entity);
        });

        getChanger().stream(request, true).forEach(entity -> {
            processParentWiring(versionDate, entity);
        });

        getSchema().datatype(request).primitive("state").set(request, ChangeState.AFFIRMED);
        em.persist(request);
    }

    @Override
    public <T extends Root> void reject(ChangeRequest<T> request) {
        checkRequestBeforeUpdate(request, "reject", ChangeState.PUBLISHED);
        getSchema().datatype(request).primitive("state").set(request, ChangeState.REJECTED);
        em.persist(request);
    }

    @Override
    public <T extends Root> void destroy(ChangeRequest<T> request) {
        checkRequestBeforeUpdate(request, "destroy", ChangeState.DRAFT);
        getChanger().stream(request, true).forEach(em::remove);
        getChanger().getUnits(request).forEach(em::remove);
        em.remove(request.getRoot());
        em.remove(request);
    }

    @Override
    public <T extends Root> void destroy(ChangeRequestSpecification<T> specification) {
        Class<ChangeRequest<T>> type = getChanger().getRequestDatatype(specification.getRoot()).getJavaType();
        ChangeRequest<T> request = find(type, specification.getUuid());
        destroy(request);
    }

    @Override
    public <T extends GlobalEntity> T find(Class<T> type, Serializable uuid) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(uuid);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(type);
        jakarta.persistence.criteria.Root<T> root = query.from(type);
        query.select(root).where(cb.equal(root.get("uuid"), uuid));
        List<T> result = em.createQuery(query).getResultList();

        return switch (result.size()) {
            case 0 -> null;
            case 1 -> result.get(0);
            default -> throw new VersioningException("too many rows - " + type.getName() + "(" + uuid + ")");
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

    private static <T extends Root> void checkRequestBeforeUpdate(ChangeRequest<T> request, String methodName,
                                                                  ChangeState desiredState) {
        Objects.requireNonNull(request);
        if (!desiredState.equals(request.getState()))
            throw new VersioningException("incorrect request state (" + request.getState() + ") for method " + methodName);
    }

    private <T extends Root> ChangeRequest<T> persistCRS(ChangeRequestSpecification<T> specification, T entity, ChangeRequest<T> request) {
        specification.getUnits().forEach(u -> {
            em.persist(u.getLeaf());


            if (u.getOperation().equals(ChangeOperation.REMOVE)) {
                Datatype<Leaf<?>> datatype = getSchema().datatype(u.getLeaf());
                datatype.primitive("version").set(u.getLeaf(), new Version(VersionState.PURGE, 0));
            }

            ChangeUnit<ChangeRequest<T>> unit = getChanger().createChangeUnit(
                    request,
                    u.getLeaf(),
                    u.getOperation()
            );
            em.persist(unit);
        });
        return request;
    }

    private <T extends Leaf<P>, P extends Versionable> void processParentWiring(long versionDate, T leaf) {
        Versionable parent = leaf.getVersion().getState().equals(VersionState.ACTIVE)
                ? em.createQuery(getActiveParentQuery(leaf)).getSingleResult()
                : null;
        getSchema().datatype(leaf).reference("parent").set(leaf, parent);
    }

    private <T extends Leaf<P>, P extends Versionable> void processHistoryIncrement(T leaf, long versionDate) {
        Datatype<T> datatype = getSchema().datatype(leaf);
        for (T activeEntity : em.createQuery(getCurrentVersionQuery(leaf)).getResultList()) {
            datatype.primitive("version").set(activeEntity, new Version(VersionState.HISTORY, 0));
            datatype.reference("parent").set(activeEntity, null);
            em.persist(activeEntity);
        }
        VersionState state = switch (leaf.getVersion().getState()) {
            case DRAFT -> VersionState.ACTIVE;
            case PURGE -> VersionState.PASSIVE;
            default -> throw new VersioningException("incorrect version.state of leaf " + leaf);
        };
        datatype.primitive("version").set(leaf, new Version(state, 0));
    }

    private <T extends Versionable> CriteriaQuery<T> getCurrentVersionQuery(T entity) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<T> type = (Class<T>) entity.getClass();

        CriteriaQuery<T> query = cb.createQuery(type);
        jakarta.persistence.criteria.Root<T> root = query.from(type);

        query.select(root).where(
                cb.equal(root.get("uuid"), entity.getUuid()),
                cb.equal(root.get("version").get("state"), VersionState.ACTIVE)
        );

        return query;
    }

    private <T extends Leaf<P>, P extends Versionable> CriteriaQuery<P> getActiveParentQuery(T entity) {
        Datatype<T> datatype = getSchema().datatype(entity);
        Parameter<T> parent = datatype.reference("parent");

        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<P> type = (Class<P>) parent.getParameterDatatype().getJavaType();

        CriteriaQuery<P> query = cb.createQuery(type);
        jakarta.persistence.criteria.Root<P> root = query.from(type);

        query.select(root).where(
                cb.equal(root.get("uuid"), entity.getParentUuid()),
                cb.equal(root.get("version").get("state"), VersionState.ACTIVE)
        );

        return query;
    }

    private <T extends GlobalEntity> T findNonNull(Class<T> type, Serializable uuid) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(uuid);

        T entity = find(type, uuid);
        if (entity == null)
            throw new VersioningException("no data found - " + type.getSimpleName() + "(" + uuid + ")");
        return entity;
    }
}