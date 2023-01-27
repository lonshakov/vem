package io.persistence.vem.engine.impl.request;

import io.persistence.vem.domain.model.Leaf;
import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.domain.request.ChangeRequest;
import io.persistence.vem.domain.request.ChangeUnit;
import io.persistence.vem.domain.request.PolymorphEntity;
import io.persistence.vem.engine.impl.crs.CRSpecificationUnitDTO;
import io.persistence.vem.spi.request.ChangeRequestSpecification;
import io.persistence.vem.spi.request.Changer;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.HistoryMapping;
import io.persistence.vem.spi.session.VersioningEntityManager;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ChangerImpl implements Changer {
    private final VersioningEntityManager vem;

    public ChangerImpl(VersioningEntityManager vem) {
        this.vem = vem;
    }

    @Override
    public <T extends Root> ChangeRequest<T> createChangeRequest(T entity) {
        ChangeRequest<T> request = getRequestDatatype(entity).instantiate();
        vem.getSchema().getDatatype(request).getReference("root").set(request, entity);
        return request;
    }

    @Override
    public <T extends Root> ChangeUnit<ChangeRequest<T>> createChangeUnit(ChangeRequest<T> request, Leaf<?> leaf, ChangeOperation operation) {
        ChangeUnit<ChangeRequest<T>> unit = getUnitDatatype(request.getRoot()).instantiate();
        Datatype<ChangeUnit<ChangeRequest<T>>> datatype = vem.getSchema().getDatatype(unit);
        datatype.getReference("request").set(unit, request);
        datatype.getPrimitive("leaf").set(unit, new PolymorphEntity(leaf.getClass(), leaf.getId()));
        datatype.getPrimitive("operation").set(unit, operation);
        return unit;
    }

    @Override
    public <T extends Root> Datatype<ChangeRequest<T>> getRequestDatatype(T entity) {
        HistoryMapping<T> mapping = (HistoryMapping<T>) vem.getFactory().getHistoryMapping().get(entity);
        return mapping.getRequestDatatype();
    }

    @Override
    public <T extends Root> Datatype<ChangeUnit<ChangeRequest<T>>> getUnitDatatype(T entity) {
        HistoryMapping<T> mapping = (HistoryMapping<T>) vem.getFactory().getHistoryMapping().get(entity);
        return mapping.getUnitDatatype();
    }

    @Override
    public <T extends Root> ChangeRequestSpecification.Unit fetch(ChangeUnit<ChangeRequest<T>> unit, boolean lazy) {
        EntityManager em = vem.em();
        Class<Leaf<?>> type = (Class<Leaf<?>>) unit.getLeaf().getType();

        Serializable id = unit.getLeaf().getId();
        Leaf<?> leaf = lazy ? em.getReference(type, id) : em.find(type, id);

        return new CRSpecificationUnitDTO(unit.getOperation(), leaf);
    }

    @Override
    public <T extends Root> Stream<ChangeRequestSpecification.Unit> stream(ChangeRequest<T> request, boolean lazy) {
        List<ChangeUnit<ChangeRequest<T>>> units = getStoredChangeUnits(request);

        Iterator<ChangeRequestSpecification.Unit> iterator = lazy
                ? units.stream().map(u -> fetch(u, true)).iterator()
                : new BatchUnitIterator(units, vem.em());

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }

    @Override
    public <T extends Root> List<ChangeUnit<ChangeRequest<T>>> getStoredChangeUnits(ChangeRequest<T> request) {
        Class<ChangeUnit<ChangeRequest<T>>> type = getUnitDatatype(request.getRoot()).getJavaType();
        CriteriaBuilder cb = vem.em().getCriteriaBuilder();

        CriteriaQuery<ChangeUnit<ChangeRequest<T>>> query =
                cb.createQuery(type);
        javax.persistence.criteria.Root<ChangeUnit<ChangeRequest<T>>> root =
                query.from(type);
        query.select(root)
                .where(cb.equal(root.get("request"), request));

        return vem.em().createQuery(query).getResultList();
    }
}
