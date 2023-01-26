package io.persistence.vem.engine.impl.crs;

import io.persistence.vem.domain.model.*;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.spi.request.ChangeRequestSpecification;
import io.persistence.vem.spi.request.ChangeRequestSpecificationBuilder;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.Parameter;
import io.persistence.vem.spi.session.VersioningEntityManager;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class CRSpecificationBuilderMerge implements ChangeRequestSpecificationBuilder {
    @Override
    public <T extends Root> ChangeRequestSpecification<T> build(T root, VersioningEntityManager vem) {
        ChangeRequestSpecification<T> specification = new CRSpecificationDTO<>(root);
        process(root, vem, specification);
        return specification;
    }

    <T extends Root, V extends Versionable> void process(V entity, VersioningEntityManager vem, ChangeRequestSpecification<T> specification) {
        if (isProcessed(entity, specification)) {
            return;
        }
        Datatype<V> datatype = vem.getSchema().datatype(entity);
        for (Parameter<V> parameter : datatype.collections().values()) {
            defineCollectionOperations(entity, vem, specification, parameter);
            for (Leaf<?> leaf : (Iterable<Leaf<?>>) parameter.get(entity)) {
                process(leaf, vem, specification);
            }
        }
        for (Parameter<V> parameter : datatype.references().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            defineReferenceOperation(entity, vem, specification, parameter);
            Leaf<?> leaf = (Leaf<?>) parameter.get(entity);
            if (leaf != null) {
                process(leaf, vem, specification);
            }
        }
    }

    private <T extends Root, V extends Versionable>
    void defineReferenceOperation(V entity, VersioningEntityManager vem, ChangeRequestSpecification<T> specification, Parameter<V> parameter) {
        Datatype<Leaf<?>> parameterDatatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();
        Serializable parentUuid = entity.getUuid();

        Leaf<?> newLeaf = (Leaf<?>) parameter.get(entity);
        Class<Leaf<?>> type = parameterDatatype.getJavaType();

        Optional<Leaf<?>> oldLeaf = fetchByParent(vem.em(), type, entity).stream().findFirst();

        if ((newLeaf == null && oldLeaf.isEmpty())
                || newLeaf != null && oldLeaf.isPresent() && newLeaf.getUuid().equals(oldLeaf.get().getUuid())) {
            return;
        }

        if (newLeaf == null) {
            Leaf<?> leaf = parameterDatatype.clone(oldLeaf.get());
            parameterDatatype.primitive("version").set(leaf, new Version(VersionState.PURGE, 0));
            parameterDatatype.primitive("parentUuid").set(leaf, parentUuid);
            parameterDatatype.reference("parent").set(leaf, null);
            specification.getUnits().add(new CRSpecificationUnitDTO(
                    ChangeOperation.REFERENCE_NULLIFY,
                    leaf
            ));
            /*CRSUtil.defineChangeOperationCascade(
                    leaf,
                    vem,
                    specification,
                    ChangeOperation.REMOVE
            );*/
            return;
        } else {
            Leaf<?> leaf = newLeaf;
            parameterDatatype.primitive("version").set(leaf, new Version(VersionState.DRAFT, 0));
            parameterDatatype.primitive("parentUuid").set(leaf, parentUuid);
            parameterDatatype.reference("parent").set(leaf, null);
            specification.getUnits().add(new CRSpecificationUnitDTO(
                    ChangeOperation.REFERENCE_REPLACE,
                    leaf
            ));
            /*CRSUtil.defineChangeOperationCascade(
                    leaf,
                    vem,
                    specification,
                    ChangeOperation.REMOVE
            );*/
        }
    }

    private <T extends Root, V extends Versionable>
    void defineCollectionOperations(V entity, VersioningEntityManager
            vem, ChangeRequestSpecification<T> specification, Parameter<V> parameter) {
        Datatype<Leaf<?>> parameterDatatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();
        Class<Leaf<?>> type = parameterDatatype.getJavaType();
        Serializable parentUuid = entity.getUuid();

        //old values
        Map<Serializable, Leaf<?>> oldLeaves = fetchByParent(vem.em(), type, entity)
                .stream()
                .collect(Collectors.toMap(GlobalEntity::getUuid, o -> o));

        //new values
        Map<Serializable, Leaf<?>> newLeaves = ((Collection<Leaf<?>>) parameter.get(entity))
                .stream()
                .collect(Collectors.toMap(GlobalEntity::getUuid, o -> o));

        //remove
        Set<Serializable> removeIndex = new HashSet<>(oldLeaves.keySet());
        removeIndex.removeAll(newLeaves.keySet());
        removeIndex.forEach(uuid -> {
            Leaf<?> leaf = parameterDatatype.clone(oldLeaves.get(uuid));
            parameterDatatype.primitive("version").set(leaf, new Version(VersionState.PURGE, 0));
            parameterDatatype.primitive("parentUuid").set(leaf, parentUuid);
            parameterDatatype.reference("parent").set(leaf, null);

            specification.getUnits().add(new CRSpecificationUnitDTO(
                    ChangeOperation.COLLECTION_REMOVE,
                    leaf
            ));
            /*CRSUtil.defineChangeOperationCascade(
                    leaf,
                    vem,
                    specification,
                    ChangeOperation.REMOVE
            );*/
        });

        //add
        Set<Serializable> addIndex = new HashSet<>(newLeaves.keySet());
        addIndex.removeAll(oldLeaves.keySet());
        addIndex.forEach(uuid -> {
            Leaf<?> leaf = newLeaves.get(uuid);
            specification.getUnits().add(new CRSpecificationUnitDTO(
                    ChangeOperation.COLLECTION_ADD,
                    leaf
            ));
            parameterDatatype.primitive("parentUuid").set(leaf, parentUuid);
            parameterDatatype.reference("parent").set(leaf, null);
            /*CRSUtil.defineChangeOperationCascade(
                    leaf,
                    vem,
                    specification,
                    ChangeOperation.ADD
            );*/
        });
    }

    <T extends Versionable> List<T> fetchByParent(EntityManager em, Class<T> type, Versionable parent) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(type);
        javax.persistence.criteria.Root<T> root = query.from(type);
        query.select(root).where(
                cb.equal(root.get("parentUuid"), parent.getUuid()),
                cb.equal(root.get("version").get("state"), VersionState.ACTIVE));
        return em.createQuery(query).getResultList();
    }

    private <T extends Root, V extends Versionable> boolean isProcessed(V
                                                                                entity, ChangeRequestSpecification<T> specification) {
        return entity instanceof Leaf<?>
                && specification
                .getUnits()
                .contains(new CRSpecificationUnitDTO(null, (Leaf<?>) entity));
    }
}
