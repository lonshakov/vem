package lsa.prototype.vem.engine.impl.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lsa.prototype.vem.model.context.ChangeOperation;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.LeafEntity;
import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.spi.request.Changer;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.session.VersioningEntityManager;
import lsa.prototype.vem.spi.schema.HistoryMapping;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ChangerImpl implements Changer {
    private final VersioningEntityManager vem;

    public ChangerImpl(VersioningEntityManager vem) {
        this.vem = vem;
    }

    @Override
    public <T extends RootEntity> ChangeRequest<T> createChangeRequest(T entity) {
        ChangeRequest<T> request = getRequestDatatype(entity).instantiate();
        request.setRoot(entity);
        return request;
    }

    @Override
    public <T extends RootEntity> ChangeUnit<ChangeRequest<T>> createChangeUnit(ChangeRequest<T> request, LeafEntity<?> leaf, ChangeOperation operation) {
        ChangeUnit<ChangeRequest<T>> unit = getUnitDatatype(request.getRoot()).instantiate();
        unit.setRequest(request);
        unit.setLeaf(leaf);
        unit.setOperation(operation);
        return unit;
    }

    @Override
    public <T extends RootEntity> Datatype<ChangeRequest<T>> getRequestDatatype(T entity) {
        HistoryMapping<T> mapping = (HistoryMapping<T>) vem.getFactory().getHistoryMapping().get(entity);
        return mapping.getRequestDatatype();
    }

    @Override
    public <T extends RootEntity> Datatype<ChangeUnit<ChangeRequest<T>>> getUnitDatatype(T entity) {
        HistoryMapping<T> mapping = (HistoryMapping<T>) vem.getFactory().getHistoryMapping().get(entity);
        return mapping.getUnitDatatype();
    }

    @Override
    public <T extends RootEntity> List<ChangeUnit<ChangeRequest<T>>> getUnits(ChangeRequest<T> request) {
        Class<ChangeUnit<ChangeRequest<T>>> type = getUnitDatatype(request.getRoot()).getJavaType();
        CriteriaBuilder cb = vem.em().getCriteriaBuilder();

        CriteriaQuery<ChangeUnit<ChangeRequest<T>>> query =
                cb.createQuery(type);
        Root<ChangeUnit<ChangeRequest<T>>> root =
                query.from(type);
        query.select(root)
                .where(cb.equal(root.get("request"), request));

        return vem.em().createQuery(query).getResultList();
    }

    @Override
    public <T extends RootEntity> LeafEntity<?> fetch(ChangeUnit<ChangeRequest<T>> unit, boolean lazy) {
        EntityManager em = vem.em();
        Class<LeafEntity<?>> type = (Class<LeafEntity<?>>) unit.getLeaf().getType();
        long id = unit.getLeaf().getId();

        return lazy ? em.getReference(type, id) : em.find(type, id);
    }

    @Override
    public <T extends RootEntity> Stream<LeafEntity<?>> stream(ChangeRequest<T> request, boolean batch) {
        if (!batch) {
            return getUnits(request).stream().map(u -> fetch(u, true));
        }
        Iterator<LeafEntity<?>> iterator = new BatchIterator(
                request,
                vem.getChanger(),
                vem.em()
        );
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
