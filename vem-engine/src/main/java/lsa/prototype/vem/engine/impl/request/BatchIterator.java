package lsa.prototype.vem.engine.impl.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lsa.prototype.vem.model.ILeafEntity;
import lsa.prototype.vem.model.IRootEntity;
import lsa.prototype.vem.request.IChangeRequest;
import lsa.prototype.vem.request.IChangeUnit;
import lsa.prototype.vem.spi.request.Changer;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchIterator implements Iterator<ILeafEntity<?>> {
    private final Iterator<Map.Entry<Class<?>, List<IChangeUnit<?>>>> batchIterator;
    private Iterator<? extends ILeafEntity<?>> objectIterator = Collections.emptyIterator();
    private final EntityManager em;

    public <T extends IRootEntity> BatchIterator(IChangeRequest<T> request, Changer changer, EntityManager em) {
        Map<Class<?>, List<IChangeUnit<?>>> source = changer.getUnits(request)
                .stream()
                .collect(Collectors.groupingBy(o -> o.getLeaf().getType()));
        this.batchIterator = source.entrySet().iterator();
        this.em = em;
    }

    @Override
    public boolean hasNext() {
        refresh();
        return objectIterator.hasNext();
    }

    @Override
    public ILeafEntity<?> next() {
        return objectIterator.next();
    }

    private void refresh() {
        if (!objectIterator.hasNext() && batchIterator.hasNext()) {
            Map.Entry<Class<?>, List<IChangeUnit<?>>> batch = batchIterator.next();

            Class<ILeafEntity<?>> type = (Class<ILeafEntity<?>>) batch.getKey();
            List<Serializable> identifiers = batch.getValue()
                    .stream()
                    .map(u -> u.getLeaf().getId())
                    .toList();

            objectIterator = fetchBatch(type, identifiers).iterator();
        }
    }

    private List<ILeafEntity<?>> fetchBatch(Class<ILeafEntity<?>> type, List<Serializable> identifiers) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ILeafEntity<?>> query = cb.createQuery(type);
        Root<ILeafEntity<?>> root = query.from(type);
        query.select(root)
                .where(root.get("id").in(identifiers));
        return em.createQuery(query).getResultList();
    }
}
