package io.persistence.vem.engine.impl.request;

import io.persistence.vem.domain.model.Leaf;
import io.persistence.vem.domain.model.Persistable;
import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.domain.request.ChangeRequest;
import io.persistence.vem.domain.request.ChangeUnit;
import io.persistence.vem.engine.impl.crs.CRSpecificationUnitDTO;
import io.persistence.vem.spi.request.ChangeRequestSpecification;
import io.persistence.vem.spi.session.VersioningEntityManager;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class BatchUnitIterator implements Iterator<ChangeRequestSpecification.Unit> {
    private final Iterator<Map.Entry<Class<?>, List<ChangeUnit<?>>>> batchIterator;
    private Iterator<ChangeRequestSpecification.Unit> unitIterator = Collections.emptyIterator();
    private final VersioningEntityManager vem;

    public <T extends Root> BatchUnitIterator(Collection<ChangeUnit<ChangeRequest<T>>> units, VersioningEntityManager vem) {
        Map<Class<?>, List<ChangeUnit<?>>> source = units
                .stream()
                .collect(Collectors.groupingBy(o -> o.getLeaf().getType()));
        this.batchIterator = source.entrySet().iterator();
        this.vem = vem;
    }

    @Override
    public boolean hasNext() {
        refresh();
        return unitIterator.hasNext();
    }

    @Override
    public ChangeRequestSpecification.Unit next() {
        return unitIterator.next();
    }

    private void refresh() {
        if (!unitIterator.hasNext() && batchIterator.hasNext()) {
            Map.Entry<Class<?>, List<ChangeUnit<?>>> batch = batchIterator.next();
            Class<Leaf<?>> type = (Class<Leaf<?>>) batch.getKey();

            Map<Serializable, ChangeOperation> operations = batch.getValue()
                    .stream()
                    .collect(Collectors.toMap(u -> u.getLeaf().getId(), ChangeUnit::getOperation));

            Set<Serializable> identifiers = operations.keySet();

            Map<Serializable, Leaf<?>> leaves = fetchBatch(type, identifiers)
                    .stream()
                    .collect(Collectors.toMap(leaf -> vem.getSchema().getUtil().getId(leaf), leaf -> leaf));

            unitIterator = identifiers.stream().map(id -> (ChangeRequestSpecification.Unit) new CRSpecificationUnitDTO(
                    operations.get(id),
                    leaves.get(id)
            )).iterator();
        }
    }

    private List<Leaf<?>> fetchBatch(Class<Leaf<?>> type, Set<Serializable> identifiers) {
        CriteriaBuilder cb = vem.em().getCriteriaBuilder();
        CriteriaQuery<Leaf<?>> query = cb.createQuery(type);
        javax.persistence.criteria.Root<Leaf<?>> root = query.from(type);
        query.select(root)
                .where(root.get("id").in(identifiers));
        return vem.em().createQuery(query).getResultList();
    }
}
