package lsa.prototype.vem.engine.impl.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lsa.prototype.vem.model.Leaf;
import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.request.ChangeRequest;
import lsa.prototype.vem.request.ChangeUnit;
import lsa.prototype.vem.request.PolymorphEntity;
import lsa.prototype.vem.spi.request.Changer;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.HistoryMapping;
import lsa.prototype.vem.spi.session.VersioningEntityManager;

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
        request.setRoot(entity);
        return request;
    }

    @Override
    public <T extends Root> ChangeUnit<ChangeRequest<T>> createChangeUnit(ChangeRequest<T> request, Leaf<?> leaf, ChangeOperation operation) {
        ChangeUnit<ChangeRequest<T>> unit = getUnitDatatype(request.getRoot()).instantiate();
        unit.setRequest(request);
        unit.setLeaf(new PolymorphEntity(leaf.getClass(), leaf.getId()));
        unit.setOperation(operation);
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
    public <T extends Root> List<ChangeUnit<ChangeRequest<T>>> getUnits(ChangeRequest<T> request) {
        Class<ChangeUnit<ChangeRequest<T>>> type = getUnitDatatype(request.getRoot()).getJavaType();
        CriteriaBuilder cb = vem.em().getCriteriaBuilder();

        CriteriaQuery<ChangeUnit<ChangeRequest<T>>> query =
                cb.createQuery(type);
        jakarta.persistence.criteria.Root<ChangeUnit<ChangeRequest<T>>> root =
                query.from(type);
        query.select(root)
                .where(cb.equal(root.get("request"), request));

        return vem.em().createQuery(query).getResultList();
    }

    @Override
    public <T extends Root> Leaf<?> fetch(ChangeUnit<ChangeRequest<T>> unit, boolean lazy) {
        EntityManager em = vem.em();
        Class<Leaf<?>> type = (Class<Leaf<?>>) unit.getLeaf().getType();
        Serializable id = unit.getLeaf().getId();

        return lazy ? em.getReference(type, id) : em.find(type, id);
    }

    @Override
    public <T extends Root> Stream<Leaf<?>> stream(ChangeRequest<T> request, boolean batch) {
        if (!batch) {
            return getUnits(request).stream().map(u -> fetch(u, true));
        }
        Iterator<Leaf<?>> iterator = new BatchIterator(
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
