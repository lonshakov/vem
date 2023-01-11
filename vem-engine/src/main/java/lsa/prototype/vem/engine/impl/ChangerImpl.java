package lsa.prototype.vem.engine.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lsa.prototype.vem.engine.spi.Changer;
import lsa.prototype.vem.engine.spi.VersioningEntityManager;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.Root;
import lsa.prototype.vem.model.version.VersionedEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ChangerImpl implements Changer {
    private final VersioningEntityManager vem;
    private final CriteriaBuilder jpaCriteriaBuilder;

    public ChangerImpl(VersioningEntityManager vem) {
        this.vem = vem;
        jpaCriteriaBuilder = vem.getFactory().getJpaFactory().getCriteriaBuilder();
    }

    public <T extends Root, R extends ChangeRequest<T>> R instantiate(T entity) {
        R request = (R) vem.getFactory().getHistoryMapping().get(entity).request().instantiate();
        request.setRoot(entity);
        return request;
    }

    public <T extends Root> Map<Class<?>, List<ChangeUnit<T>>> fetchUnits(ChangeRequest<T> request) {
        Class<?> unitType = vem.getHistoryMapping()
                .get(request.getRoot())
                .unit()
                .getJavaType();

        CriteriaQuery<Object> query = cb().createQuery();
        jakarta.persistence.criteria.Root<?> root = query.from(unitType);

        query.select(root)
                .where(cb().equal(root.get("request"), request));

        return vem.em().createQuery(query).getResultList().stream()
                .map(o -> (ChangeUnit<T>) o)
                .collect(Collectors.groupingBy(o -> o.getLeaf().getType()));
    }

    public <T extends Root> Map<Class<?>, List<VersionedEntity>> fetchLeaves(ChangeRequest<T> request) {
        return fetchUnits(request).entrySet().stream().map(bucket -> {
            Set<Long> identifiers = bucket.getValue().stream().map(u -> u.getLeaf().getId()).collect(Collectors.toSet());

            CriteriaQuery<Object> query = cb().createQuery();
            jakarta.persistence.criteria.Root<?> root = query.from(bucket.getKey());

            query.select(root).where(root.get("id").in(identifiers));
            Object results = vem.em().createQuery(query).getResultList();

            return Map.entry(bucket.getKey(), (List<VersionedEntity>) results);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private CriteriaBuilder cb() {
        return jpaCriteriaBuilder;
    }
}
