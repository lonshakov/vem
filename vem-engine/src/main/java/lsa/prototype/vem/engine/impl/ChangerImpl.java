package lsa.prototype.vem.engine.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lsa.prototype.vem.engine.spi.Changer;
import lsa.prototype.vem.engine.spi.VersioningEntityManager;
import lsa.prototype.vem.engine.spi.schema.HistoryMapping;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.Leaf;
import lsa.prototype.vem.model.version.Root;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangerImpl implements Changer {
    private final VersioningEntityManager vem;
    private final CriteriaBuilder jpaCriteriaBuilder;

    public ChangerImpl(VersioningEntityManager vem) {
        this.vem = vem;
        jpaCriteriaBuilder = vem.getFactory().getJpaFactory().getCriteriaBuilder();
    }

    public <T extends Root> ChangeRequest<T> instantiate(T entity) {
        ChangeRequest<T> request = (ChangeRequest<T>) vem.getFactory().getHistoryMapping().get(entity).getRequestDatatype().instantiate();
        request.setRoot(entity);
        return request;
    }

    @Override
    public <T extends Root> Stream<Leaf<?>> stream(ChangeRequest<T> request) {
        return getUnits(request).stream()
                .map(ChangeUnit::getLeaf)
                .map(o -> (Leaf<?>) vem.em().getReference(o.getType(), o.getId()));
    }

    @Override
    public <T extends Root>
    Map<Class<?>, List<Leaf<?>>> map(ChangeRequest<T> request) {
        return mapTypedUnits(request).entrySet().stream().map(bucket -> {
            Class<Leaf<?>> leafType = (Class<Leaf<?>>) bucket.getKey();
            Set<Long> identifiers = bucket.getValue().stream().map(u -> u.getLeaf().getId()).collect(Collectors.toSet());

            CriteriaQuery<Leaf<?>> query = cb().createQuery(leafType);
            jakarta.persistence.criteria.Root<Leaf<?>> root = query.from(leafType);

            query.select(root).where(root.get("id").in(identifiers));
            List<Leaf<?>> results = vem.em().createQuery(query).getResultList();

            return Map.entry(bucket.getKey(), results);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private CriteriaBuilder cb() {
        return jpaCriteriaBuilder;
    }

    private <T extends Root>
    Map<Class<?>, List<ChangeUnit<ChangeRequest<T>>>> mapTypedUnits(ChangeRequest<T> request) {
        return getUnits(request)
                .stream()
                .collect(Collectors.groupingBy(o -> o.getLeaf().getType()));
    }

    private <T extends Root>
    List<ChangeUnit<ChangeRequest<T>>> getUnits(ChangeRequest<T> request) {
        return vem.em()
                .createQuery(getUnitQuery(request))
                .getResultList();
    }

    public <T extends Root>
    CriteriaQuery<ChangeUnit<ChangeRequest<T>>> getUnitQuery(ChangeRequest<T> request) {
        HistoryMapping<T> historyMapping = (HistoryMapping<T>) vem.getHistoryMappings().get(request.getRoot());

        Class<ChangeUnit<ChangeRequest<T>>> unitType = historyMapping
                .getUnitDatatype()
                .getJavaType();

        CriteriaQuery<ChangeUnit<ChangeRequest<T>>> query =
                cb().createQuery(unitType);

        jakarta.persistence.criteria.Root<ChangeUnit<ChangeRequest<T>>> root =
                query.from(unitType);

        query.select(root).where(cb().equal(root.get("request"), request));
        return query;
    }
}
