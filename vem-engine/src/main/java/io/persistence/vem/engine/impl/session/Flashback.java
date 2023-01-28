package io.persistence.vem.engine.impl.session;

import io.persistence.vem.domain.model.Leaf;
import io.persistence.vem.domain.model.VersionState;
import io.persistence.vem.domain.model.Versionable;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.Parameter;
import io.persistence.vem.spi.schema.Schema;
import io.persistence.vem.spi.session.VersioningEntityManager;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Flashback {
    private final EntityManager em;
    private final Schema schema;

    public Flashback(VersioningEntityManager vem) {
        this.em = vem.em();
        this.schema = vem.getSchema();
    }

    public <T> T find(Class<T> type, Serializable uuid, LocalDateTime dateTime) {
        Datatype<T> datatype = schema.getDatatype(type);

        T entity = find1byUuid(uuid, datatype.getGlobalIdentifier());

        return entity;
    }

    private <T> T find0(Serializable uuid, Datatype<T> datatype) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = cb.createQuery(datatype.getJavaType());
        Root<T> root = criteriaQuery.from(datatype.getJavaType());
        criteriaQuery.select(root).where(cb.equal(
                root.get(datatype.getGlobalIdentifier().getName()),
                uuid
        ));

        TypedQuery<T> typedQuery = em.createQuery(criteriaQuery);

        EntityGraph<T> entityGraph = graph(datatype);
        typedQuery.setHint("javax.persistence.fetchgraph", entityGraph);

        return typedQuery.getSingleResult();
    }

    private <T> EntityGraph<T> graph(Datatype<T> datatype) {
        EntityGraph<T> graph = em.createEntityGraph(datatype.getJavaType());
        datatype.getPrimitives().values().stream()
                .filter(Parameter::isPrimitive)
                .map(Parameter::getAttribute)
                .forEach(graph::addAttributeNodes);
        return graph;
    }

    private <T> T find1byUuid(Serializable uuid, Parameter<T> uuidParameter) {
        Datatype<T> datatype = uuidParameter.getStructureDatatype();

        List<Parameter<T>> parameters = datatype.getPrimitives().values().stream()
                .filter(Parameter::isPrimitive)
                .collect(Collectors.toList());
        parameters.add(datatype.getGlobalIdentifier());
        parameters.add(datatype.getIdentifier());

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
        Root<T> root = criteriaQuery.from(datatype.getJavaType());

        List<Selection<?>> selections = parameters.stream()
                .map(Parameter::getName)
                .map(name -> root.get(name).alias(name))
                .collect(Collectors.toList());

        criteriaQuery.multiselect(selections).where(
                cb.equal(root.get(uuidParameter.getName()), uuid),
                cb.equal(root.get("version").get("state"), VersionState.ACTIVE)
        );

        Tuple tuple = em.createQuery(criteriaQuery).getSingleResult();

        T entity = datatype.instantiate();

        parameters.forEach(parameter -> parameter.set(entity, tuple.get(parameter.getName())));

        Serializable nextUuid = (Serializable) datatype.getGlobalIdentifier().get(entity);

        datatype.getReferences().values().forEach(reference -> {
            if (reference.isVersionable()) {
                if (Leaf.class.isAssignableFrom(reference.getJavaType())) {
                    //Leaf
                    Datatype<Leaf<?>> refDatatype = (Datatype<Leaf<?>>) reference.getParameterDatatype();
                    Leaf<?> leaf = find1byUuid(
                            nextUuid,
                            refDatatype.getPrimitive("parentUuid")
                    );
                    reference.set(entity, leaf);
                    refDatatype.getReference("parent").set(leaf, entity);
                } else {
                    //Root
                    //todo
                }
            } else {
                //nonVersionable
                //todo
            }
        });

        return entity;
    }
}
