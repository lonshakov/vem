package lsa.prototype.vem.engine.impl.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lsa.prototype.vem.model.Leaf;
import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.request.ChangeRequest;
import lsa.prototype.vem.request.ChangeUnit;
import lsa.prototype.vem.spi.request.Changer;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchIterator implements Iterator<Leaf<?>> {
    private final Iterator<Map.Entry<Class<?>, List<ChangeUnit<?>>>> batchIterator;
    private Iterator<? extends Leaf<?>> objectIterator = Collections.emptyIterator();
    private final EntityManager em;

    public <T extends Root> BatchIterator(ChangeRequest<T> request, Changer changer, EntityManager em) {
        Map<Class<?>, List<ChangeUnit<?>>> source = changer.getUnits(request)
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
    public Leaf<?> next() {
        return objectIterator.next();
    }

    private void refresh() {
        if (!objectIterator.hasNext() && batchIterator.hasNext()) {
            Map.Entry<Class<?>, List<ChangeUnit<?>>> batch = batchIterator.next();

            Class<Leaf<?>> type = (Class<Leaf<?>>) batch.getKey();
            List<Serializable> identifiers = batch.getValue()
                    .stream()
                    .map(u -> u.getLeaf().getId())
                    .toList();

            objectIterator = fetchBatch(type, identifiers).iterator();
        }
    }

    private List<Leaf<?>> fetchBatch(Class<Leaf<?>> type, List<Serializable> identifiers) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Leaf<?>> query = cb.createQuery(type);
        jakarta.persistence.criteria.Root<Leaf<?>> root = query.from(type);
        query.select(root)
                .where(root.get("id").in(identifiers));
        return em.createQuery(query).getResultList();
    }
}
