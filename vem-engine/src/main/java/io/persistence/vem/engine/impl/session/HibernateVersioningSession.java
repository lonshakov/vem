package io.persistence.vem.engine.impl.session;

import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.domain.request.ChangeRequest;
import io.persistence.vem.domain.request.ChangeState;
import io.persistence.vem.domain.request.ChangeUnit;
import io.persistence.vem.engine.impl.crs.CRSpecificationBuilderCascade;
import io.persistence.vem.engine.impl.crs.CRSpecificationBuilderMerge;
import io.persistence.vem.engine.impl.function.GraphBinderImpl;
import io.persistence.vem.engine.impl.function.HistoryRecorderImpl;
import io.persistence.vem.engine.impl.function.Util;
import io.persistence.vem.engine.impl.request.ChangerImpl;
import io.persistence.vem.spi.function.GraphBinder;
import io.persistence.vem.spi.function.HistoryRecorder;
import io.persistence.vem.spi.VersioningException;
import io.persistence.vem.spi.context.SessionContext;
import io.persistence.vem.spi.function.VisitorContext;
import io.persistence.vem.spi.request.ChangeRequestSpecification;
import io.persistence.vem.spi.request.Changer;
import io.persistence.vem.spi.session.VersioningEntityManager;
import io.persistence.vem.spi.session.VersioningEntityManagerFactory;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class HibernateVersioningSession implements VersioningEntityManager {
    private final VersioningEntityManagerFactory factory;
    private final EntityManager em;
    private final Changer changer;
    private final SessionContext context;
    private final HistoryRecorder historyRecorder;
    private final GraphBinder graphBinder;

    public HibernateVersioningSession(VersioningEntityManagerFactory factory, EntityManager em, SessionContext context) {
        this.factory = factory;
        this.em = em;
        this.context = context;
        changer = new ChangerImpl(this);
        historyRecorder = new HistoryRecorderImpl(this);
        graphBinder = new GraphBinderImpl(this);
    }

    @Override
    public <T extends Root> ChangeRequest<T> persist(T entity) {
        ChangeRequestSpecification<T> crs = new CRSpecificationBuilderCascade<T>(ChangeOperation.CASCADE_CREATE, this)
                .build(null, entity);

        return persist(crs);
    }

    @Override
    public <T extends Root> ChangeRequest<T> persist(ChangeRequestSpecification<T> specification) {
        Objects.requireNonNull(specification);

        T entity = specification.getRoot();
        Objects.requireNonNull(entity);

        em.persist(entity);

        ChangeRequest<T> request = getChanger().createChangeRequest(entity);
        em.persist(request);

        return persistCRS(specification, request);
    }

    @Override
    public <T extends Root> ChangeRequest<T> merge(T entity) {
        T clone = getSchema().getDatatype(entity).clone(entity);
        cascade(entity, (obj, ctx) -> em().detach(obj));
        ChangeRequestSpecification<T> crs = new CRSpecificationBuilderMerge<T>(this).build(null, clone);

        return merge(crs);
    }

    @Override
    public <T extends Root> ChangeRequest<T> merge(ChangeRequestSpecification<T> specification) {
        Objects.requireNonNull(specification);

        T entity = findNonNull(
                (Class<T>) specification.getRoot().getClass(),
                getSchema().getUtil().getUuid(specification.getRoot())
        );

        ChangeRequest<T> request;
        if (specification.getUuid() == null) {
            request = getChanger().createChangeRequest(entity);
            em.persist(request);
        } else {
            Class<ChangeRequest<T>> type = getChanger().getRequestDatatype(entity).getJavaType();
            request = findNonNull(type, specification.getUuid());
        }
        return persistCRS(specification, request);
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
        getSchema().getDatatype(request).getPrimitive("state").set(request, ChangeState.PUBLISHED);
        em.merge(request);
    }

    @Override
    public <T extends Root> void affirm(ChangeRequest<T> request) {
        checkRequestBeforeUpdate(request, "affirm", ChangeState.PUBLISHED);

        historyRecorder.record(request);
        graphBinder.bind(request);

        getSchema().getDatatype(request).getPrimitive("state").set(request, ChangeState.AFFIRMED);
        em.merge(request);
    }

    @Override
    public <T extends Root> void reject(ChangeRequest<T> request) {
        checkRequestBeforeUpdate(request, "reject", ChangeState.PUBLISHED);
        getSchema().getDatatype(request).getPrimitive("state").set(request, ChangeState.REJECTED);
        em.merge(request);
    }

    @Override
    public <T extends Root> void destroy(ChangeRequest<T> request) {
        checkRequestBeforeUpdate(request, "destroy", ChangeState.DRAFT);
        //remove leaves
        getChanger().stream(request, true).map(ChangeRequestSpecification.Unit::getLeaf).forEach(em::remove);
        //remove units
        getChanger().getStoredChangeUnits(request).forEach(em::remove);

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
    public <T> T find(Class<T> type, Serializable uuid) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(uuid);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(type);
        javax.persistence.criteria.Root<T> root = query.from(type);
        query.select(root).where(cb.equal(
                root.get(getSchema().getDatatype(type).getGlobalIdentifier().getName()),
                uuid
        ));

        List<T> result = em.createQuery(query).getResultList();
        return switch (result.size()) {
            case 0 -> null;
            case 1 -> result.get(0);
            default -> throw new VersioningException("too many rows - " + type.getName() + "(" + uuid + ")");
        };
    }

    @Override
    public <T> void cascade(T entity, BiConsumer<Object, VisitorContext> task) {
        Util.VisitorContextImpl ctx = new Util.VisitorContextImpl(this);
        Util.walk(entity, ctx, task);
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

    private <T extends Root> ChangeRequest<T> persistCRS(ChangeRequestSpecification<T> specification, ChangeRequest<T> request) {
        specification.getUnits().forEach(u -> {
            em.persist(u.getLeaf());
            ChangeUnit<ChangeRequest<T>> unit = getChanger().createChangeUnit(
                    request,
                    u.getLeaf(),
                    u.getOperation()
            );
            em.persist(unit);
        });
        return request;
    }

    private <T> T findNonNull(Class<T> type, Serializable uuid) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(uuid);

        T entity = find(type, uuid);
        if (entity == null)
            throw new VersioningException("no data found - " + type.getSimpleName() + "(" + uuid + ")");
        return entity;
    }
}