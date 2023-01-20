package lsa.prototype.vem.engine.impl.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.LeafEntity;
import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.spi.request.Changer;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchIterator implements Iterator<LeafEntity<?>> {
    private final Iterator<Map.Entry<Class<?>, List<ChangeUnit<?>>>> batchIterator;
    private Iterator<? extends LeafEntity<?>> objectIterator = Collections.emptyIterator();
    private final EntityManager em;

    public <T extends RootEntity> BatchIterator(ChangeRequest<T> request, Changer changer, EntityManager em) {
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
    public LeafEntity<?> next() {
        return objectIterator.next();
    }

    private void refresh() {
        if (!objectIterator.hasNext() && batchIterator.hasNext()) {
            Map.Entry<Class<?>, List<ChangeUnit<?>>> batch = batchIterator.next();

            Class<LeafEntity<?>> type = (Class<LeafEntity<?>>) batch.getKey();
            List<Long> identifiers = batch.getValue()
                    .stream()
                    .map(u -> u.getLeaf().getId())
                    .toList();

            objectIterator = fetchBatch(type, identifiers).iterator();
        }
    }

    private List<LeafEntity<?>> fetchBatch(Class<LeafEntity<?>> type, List<Long> identifiers) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LeafEntity<?>> query = cb.createQuery(type);
        Root<LeafEntity<?>> root = query.from(type);
        query.select(root)
                .where(root.get("id").in(identifiers));
        return em.createQuery(query).getResultList();
    }
}
