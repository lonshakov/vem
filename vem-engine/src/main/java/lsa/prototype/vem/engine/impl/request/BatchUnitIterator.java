package lsa.prototype.vem.engine.impl.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lsa.prototype.vem.engine.impl.crs.CRSpecificationUnitDTO;
import lsa.prototype.vem.model.Leaf;
import lsa.prototype.vem.model.Persistable;
import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.request.ChangeRequest;
import lsa.prototype.vem.request.ChangeUnit;
import lsa.prototype.vem.spi.request.ChangeRequestSpecification;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class BatchUnitIterator implements Iterator<ChangeRequestSpecification.Unit> {
    private final Iterator<Map.Entry<Class<?>, List<ChangeUnit<?>>>> batchIterator;
    private Iterator<ChangeRequestSpecification.Unit> unitIterator = Collections.emptyIterator();
    private final EntityManager em;

    public <T extends Root> BatchUnitIterator(Collection<ChangeUnit<ChangeRequest<T>>> units, EntityManager em) {
        Map<Class<?>, List<ChangeUnit<?>>> source = units
                .stream()
                .collect(Collectors.groupingBy(o -> o.getLeaf().getType()));
        this.batchIterator = source.entrySet().iterator();
        this.em = em;
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
                    .collect(Collectors.toMap(Persistable::getId, leaf -> leaf));

            unitIterator = identifiers.stream().map(id -> (ChangeRequestSpecification.Unit) new CRSpecificationUnitDTO(
                    operations.get(id),
                    leaves.get(id)
            )).iterator();
        }
    }

    private List<Leaf<?>> fetchBatch(Class<Leaf<?>> type, Set<Serializable> identifiers) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Leaf<?>> query = cb.createQuery(type);
        jakarta.persistence.criteria.Root<Leaf<?>> root = query.from(type);
        query.select(root)
                .where(root.get("id").in(identifiers));
        return em.createQuery(query).getResultList();
    }
}
