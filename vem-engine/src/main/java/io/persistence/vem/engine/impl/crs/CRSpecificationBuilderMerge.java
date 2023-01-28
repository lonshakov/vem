package io.persistence.vem.engine.impl.crs;

import io.persistence.vem.domain.model.*;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.spi.request.ChangeRequestSpecification;
import io.persistence.vem.spi.request.ChangeRequestSpecificationBuilder;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.PluralParameter;
import io.persistence.vem.spi.schema.SingularParameter;
import io.persistence.vem.spi.session.VersioningEntityManager;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CRSpecificationBuilderMerge<T extends Root> implements ChangeRequestSpecificationBuilder<T> {
    private VersioningEntityManager vem;
    private ChangeRequestSpecification<T> specification;
    private Set<Serializable> passedUuids = new HashSet<>();

    public CRSpecificationBuilderMerge(VersioningEntityManager vem) {
        this.vem = vem;
    }

    @Override
    public ChangeRequestSpecification<T> build(Serializable uuid, T root) {
        this.vem = vem;
        specification = new CRSpecificationDTO<>(uuid, root);
        process(root);
        return specification;
    }

    <V extends Versionable> void process(V entity) {
        Serializable entityUuid = vem.getSchema().getUtil().getUuid(entity);
        if (passedUuids.contains(entityUuid))
            return;
        passedUuids.add(entityUuid);

        Datatype<V> datatype = vem.getSchema().getDatatype(entity);
        for (PluralParameter<V> parameter : datatype.getCollections().values()) {
            defineCollectionOperations(entity, parameter);
            parameter.get(entity).forEach(obj -> process((Leaf<?>) obj));
        }
        for (SingularParameter<V> parameter : datatype.getReferences().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            defineReferenceOperation(entity, parameter);
            Leaf<?> leaf = parameter.get(entity);
            if (leaf != null) {
                process(leaf);
            }
        }
    }

    private <V extends Versionable> void defineReferenceOperation(V entity, SingularParameter<V> parameter) {
        Datatype<Leaf<?>> parameterDatatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();
        Serializable parentUuid = vem.getSchema().getUtil().getUuid(entity);

        Leaf<?> newLeaf = parameter.get(entity);
        Class<Leaf<?>> type = parameterDatatype.getJavaType();

        Optional<Leaf<?>> oldLeaf = fetchByParentUuid(vem.em(), type, parentUuid).stream().findFirst();

        if ((newLeaf == null && oldLeaf.isEmpty())
                || oldLeaf.isPresent() && vem.getSchema().getUtil().equals(oldLeaf.get(), newLeaf)) {
            return;
        }
        if (newLeaf == null) {
            Leaf<?> leaf = parameterDatatype.clone(oldLeaf.get());
            parameterDatatype.getPrimitive("version").set(leaf, new Version(VersionState.PURGE, vem.getSessionContext().getUser().getLogin()));
            parameterDatatype.getPrimitive("parentUuid").set(leaf, parentUuid);
            parameterDatatype.getReference("parent").set(leaf, null);
            specification.getUnits().add(new CRSpecificationUnitDTO(
                    ChangeOperation.REFERENCE_NULLIFY,
                    leaf
            ));
        } else {
            Leaf<?> leaf = newLeaf;
            parameterDatatype.getPrimitive("version").set(leaf, new Version(VersionState.DRAFT, vem.getSessionContext().getUser().getLogin()));
            parameterDatatype.getPrimitive("parentUuid").set(leaf, parentUuid);
            parameterDatatype.getReference("parent").set(leaf, null);
            specification.getUnits().add(new CRSpecificationUnitDTO(
                    ChangeOperation.REFERENCE_REPLACE,
                    leaf
            ));
        }
    }

    private <V extends Versionable> void defineCollectionOperations(V entity, PluralParameter<V> parameter) {
        Datatype<Leaf<?>> parameterDatatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();
        Class<Leaf<?>> type = parameterDatatype.getJavaType();
        Serializable parentUuid = vem.getSchema().getUtil().getUuid(entity);

        //old values
        Map<Serializable, Leaf<?>> oldLeaves = fetchByParentUuid(vem.em(), type, parentUuid)
                .stream()
                .collect(Collectors.toMap(o -> vem.getSchema().getUtil().getUuid(o), o -> o));

        //new values
        Map<Serializable, Leaf<?>> newLeaves = parameter.get(entity)
                .stream()
                .map(obj -> (Leaf<?>) obj)
                .collect(Collectors.toMap(o -> vem.getSchema().getUtil().getUuid(o), o -> o));

        //remove
        Set<Serializable> removeIndex = new HashSet<>(oldLeaves.keySet());
        removeIndex.removeAll(newLeaves.keySet());
        removeIndex.forEach(uuid -> {
            Leaf<?> leaf = parameterDatatype.clone(oldLeaves.get(uuid));
            parameterDatatype.getPrimitive("version").set(leaf, new Version(VersionState.PURGE, vem.getSessionContext().getUser().getLogin()));
            parameterDatatype.getPrimitive("parentUuid").set(leaf, parentUuid);
            parameterDatatype.getReference("parent").set(leaf, null);

            specification.getUnits().add(new CRSpecificationUnitDTO(
                    ChangeOperation.COLLECTION_REMOVE,
                    leaf
            ));
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
            parameterDatatype.getPrimitive("parentUuid").set(leaf, parentUuid);
            parameterDatatype.getReference("parent").set(leaf, null);
        });
    }

    <T extends Versionable> List<T> fetchByParentUuid(EntityManager em, Class<T> type, Serializable parentUuid) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(type);
        javax.persistence.criteria.Root<T> root = query.from(type);
        query.select(root).where(
                cb.equal(root.get("parentUuid"), parentUuid),
                cb.equal(root.get("version").get("state"), VersionState.ACTIVE));
        return em.createQuery(query).getResultList();
    }
}
