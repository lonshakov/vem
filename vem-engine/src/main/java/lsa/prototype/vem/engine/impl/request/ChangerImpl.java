package lsa.prototype.vem.engine.impl.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lsa.prototype.vem.model.ILeafEntity;
import lsa.prototype.vem.model.IRootEntity;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.request.IChangeRequest;
import lsa.prototype.vem.request.IChangeUnit;
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
    public <T extends IRootEntity> IChangeRequest<T> createChangeRequest(T entity) {
        IChangeRequest<T> request = getRequestDatatype(entity).instantiate();
        request.setRoot(entity);
        return request;
    }

    @Override
    public <T extends IRootEntity> IChangeUnit<IChangeRequest<T>> createChangeUnit(IChangeRequest<T> request, ILeafEntity<?> leaf, ChangeOperation operation) {
        IChangeUnit<IChangeRequest<T>> unit = getUnitDatatype(request.getRoot()).instantiate();
        unit.setRequest(request);
        unit.setLeaf(new PolymorphEntity(leaf.getClass(), leaf.getId()));
        unit.setOperation(operation);
        return unit;
    }

    @Override
    public <T extends IRootEntity> Datatype<IChangeRequest<T>> getRequestDatatype(T entity) {
        HistoryMapping<T> mapping = (HistoryMapping<T>) vem.getFactory().getHistoryMapping().get(entity);
        return mapping.getRequestDatatype();
    }

    @Override
    public <T extends IRootEntity> Datatype<IChangeUnit<IChangeRequest<T>>> getUnitDatatype(T entity) {
        HistoryMapping<T> mapping = (HistoryMapping<T>) vem.getFactory().getHistoryMapping().get(entity);
        return mapping.getUnitDatatype();
    }

    @Override
    public <T extends IRootEntity> List<IChangeUnit<IChangeRequest<T>>> getUnits(IChangeRequest<T> request) {
        Class<IChangeUnit<IChangeRequest<T>>> type = getUnitDatatype(request.getRoot()).getJavaType();
        CriteriaBuilder cb = vem.em().getCriteriaBuilder();

        CriteriaQuery<IChangeUnit<IChangeRequest<T>>> query =
                cb.createQuery(type);
        Root<IChangeUnit<IChangeRequest<T>>> root =
                query.from(type);
        query.select(root)
                .where(cb.equal(root.get("request"), request));

        return vem.em().createQuery(query).getResultList();
    }

    @Override
    public <T extends IRootEntity> ILeafEntity<?> fetch(IChangeUnit<IChangeRequest<T>> unit, boolean lazy) {
        EntityManager em = vem.em();
        Class<ILeafEntity<?>> type = (Class<ILeafEntity<?>>) unit.getLeaf().getType();
        Serializable id = unit.getLeaf().getId();

        return lazy ? em.getReference(type, id) : em.find(type, id);
    }

    @Override
    public <T extends IRootEntity> Stream<ILeafEntity<?>> stream(IChangeRequest<T> request, boolean batch) {
        if (!batch) {
            return getUnits(request).stream().map(u -> fetch(u, true));
        }
        Iterator<ILeafEntity<?>> iterator = new BatchIterator(
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
