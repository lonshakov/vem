package lsa.prototype.vem.engine.impl.lab;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import lsa.prototype.vem.engine.impl.request.CRSpecificationDTO;
import lsa.prototype.vem.engine.impl.request.CRUnitDTO;
import lsa.prototype.vem.model.*;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.spi.request.ChangeRequestSpecification;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;
import lsa.prototype.vem.spi.session.VersioningEntityManager;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class CRSMergeBuilder implements ChangeRequestSpecificationBuilder {
    @Override
    public <T extends Root> ChangeRequestSpecification<T> build(T root, VersioningEntityManager vem) {
        ChangeRequestSpecification<T> specification = new CRSpecificationDTO<>(root);
        process(root, vem, specification);
        return specification;
    }

    <T extends Root, V extends Versionable> void process(V entity, VersioningEntityManager vem, ChangeRequestSpecification<T> specification) {
        Datatype<V> datatype = vem.getSchema().datatype(entity);
        for (Parameter<V> parameter : datatype.collections().values()) {
            defineCollectionOperations(entity, vem, specification, parameter);
            for (Leaf<?> leaf : (Iterable<Leaf<?>>) parameter.get(entity)) {
                //if (leaf.getId() != null)

                process(leaf, vem, specification);
            }
        }
        for (Parameter<V> parameter : datatype.references().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            defineReferenceOperation(entity, vem, specification, parameter);

            Leaf<?> leaf = (Leaf<?>) parameter.get(entity);
            if (leaf != null && leaf.getId() != null) {
                process(leaf, vem, specification);
            }
        }
    }

    private <T extends Root, V extends Versionable>
    void defineReferenceOperation(V entity, VersioningEntityManager vem, ChangeRequestSpecification<T> specification, Parameter<V> parameter) {
        Datatype<Leaf<?>> parameterDatatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();
        Serializable parentUuid = entity.getUuid();

        Leaf<Versionable> newLeaf = (Leaf<Versionable>) parameter.get(entity);
        Class<Leaf<?>> type = parameterDatatype.getJavaType();

        Optional<Leaf<?>> oldLeaf = fetchByParent(vem.em(), type, entity).stream().findFirst();

        if (newLeaf == null && oldLeaf.isPresent()) {
            //remove
            specification.getUnits().add(new CRUnitDTO(
                    ChangeOperation.REMOVE,
                    oldLeaf.get()
            ));
            parameterDatatype.primitive("parentUuid").set(newLeaf, parentUuid);
            CRSUtil.defineChangeOperationCascade(
                    oldLeaf.get(),
                    vem,
                    specification,
                    ChangeOperation.REMOVE
            );
        } else if (newLeaf != null && (oldLeaf.isEmpty() || !oldLeaf.get().getUuid().equals(newLeaf.getUuid()))) {
            //add
            specification.getUnits().add(new CRUnitDTO(
                    ChangeOperation.ADD,
                    newLeaf
            ));
            parameterDatatype.primitive("parentUuid").set(newLeaf, parentUuid);
            CRSUtil.defineChangeOperationCascade(
                    newLeaf,
                    vem,
                    specification,
                    ChangeOperation.ADD
            );
        }
    }

    private <T extends Root, V extends Versionable>
    void defineCollectionOperations(V entity, VersioningEntityManager vem, ChangeRequestSpecification<T> specification, Parameter<V> parameter) {
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
        removeIndex.forEach(id -> {
            Leaf<?> leaf = oldLeaves.get(id);
            specification.getUnits().add(new CRUnitDTO(
                    ChangeOperation.REMOVE,
                    leaf
            ));
            parameterDatatype.primitive("parentUuid").set(leaf, parentUuid);
            parameterDatatype.reference("parent").set(leaf, null);
            CRSUtil.defineChangeOperationCascade(
                    leaf,
                    vem,
                    specification,
                    ChangeOperation.REMOVE
            );
        });

        //add
        Set<Serializable> addIndex = new HashSet<>(newLeaves.keySet());
        addIndex.removeAll(oldLeaves.keySet());
        addIndex.forEach(id -> {
            Leaf<?> leaf = newLeaves.get(id);
            specification.getUnits().add(new CRUnitDTO(
                    ChangeOperation.ADD,
                    leaf
            ));
            parameterDatatype.primitive("parentUuid").set(leaf, parentUuid);
            parameterDatatype.reference("parent").set(leaf, null);
            CRSUtil.defineChangeOperationCascade(
                    leaf,
                    vem,
                    specification,
                    ChangeOperation.ADD
            );
        });
    }

    <T extends Versionable> List<T> fetchByParent(EntityManager em, Class<T> type, Versionable parent) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(type);
        jakarta.persistence.criteria.Root<T> root = query.from(type);
        query.select(root).where(
                cb.equal(root.get("parentUuid"), parent.getUuid()),
                cb.equal(root.get("version").get("state"), VersionState.ACTIVE));
        return em.createQuery(query).getResultList();
    }
}
