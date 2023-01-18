package lsa.prototype.vem.engine.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.LeafEntity;
import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.spi.Changer;
import lsa.prototype.vem.spi.VersioningEntityManager;
import lsa.prototype.vem.spi.schema.HistoryMapping;

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

    public <T extends RootEntity> ChangeRequest<T> instantiate(T entity) {
        ChangeRequest<T> request = (ChangeRequest<T>) vem.getFactory().getHistoryMapping().get(entity).getRequestDatatype().instantiate();
        request.setRoot(entity);
        return request;
    }

    @Override
    public <T extends RootEntity> Stream<LeafEntity<?>> stream(ChangeRequest<T> request) {
        return getUnits(request).stream()
                .map(ChangeUnit::getLeaf)
                .map(o -> (LeafEntity<?>) vem.em().getReference(o.getType(), o.getId()));
    }

    @Override
    public <T extends RootEntity>
    Map<Class<?>, List<LeafEntity<?>>> map(ChangeRequest<T> request) {
        return mapTypedUnits(request).entrySet().stream().map(bucket -> {
            Class<LeafEntity<?>> leafType = (Class<LeafEntity<?>>) bucket.getKey();
            Set<Long> identifiers = bucket.getValue().stream().map(u -> u.getLeaf().getId()).collect(Collectors.toSet());

            CriteriaQuery<LeafEntity<?>> query = cb().createQuery(leafType);
            jakarta.persistence.criteria.Root<LeafEntity<?>> root = query.from(leafType);

            query.select(root).where(root.get("id").in(identifiers));
            List<LeafEntity<?>> results = vem.em().createQuery(query).getResultList();

            return Map.entry(bucket.getKey(), results);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private CriteriaBuilder cb() {
        return jpaCriteriaBuilder;
    }

    private <T extends RootEntity>
    Map<Class<?>, List<ChangeUnit<ChangeRequest<T>>>> mapTypedUnits(ChangeRequest<T> request) {
        return getUnits(request)
                .stream()
                .collect(Collectors.groupingBy(o -> o.getLeaf().getType()));
    }

    private <T extends RootEntity>
    List<ChangeUnit<ChangeRequest<T>>> getUnits(ChangeRequest<T> request) {
        return vem.em()
                .createQuery(getUnitQuery(request))
                .getResultList();
    }

    public <T extends RootEntity>
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
