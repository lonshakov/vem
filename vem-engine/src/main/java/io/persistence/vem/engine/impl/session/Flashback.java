package io.persistence.vem.engine.impl.session;

import io.persistence.vem.domain.model.Leaf;
import io.persistence.vem.spi.VersioningException;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.Parameter;
import io.persistence.vem.spi.schema.Schema;
import io.persistence.vem.spi.schema.SingularParameter;
import io.persistence.vem.spi.session.VersioningEntityManager;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.io.Serializable;
import java.time.LocalDateTime;
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


        T entity = selectByUuid(uuid, datatype.getGlobalIdentifier(), dateTime).get(0);
        fetchGraph(entity, dateTime);

        return entity;
    }

    private <T> void fetchGraph(T entity, LocalDateTime dateTime) {
        Datatype<T> datatype = schema.getDatatype(entity);

        Serializable nextUuid = datatype.getGlobalIdentifier().get(entity);

        datatype.getReferences().values().stream().filter(r -> !r.getName().equals("parent")).forEach(reference -> {
            if (reference.isVersionable()) {
                if (Leaf.class.isAssignableFrom(reference.getGraphType())) {
                    //Leaf
                    Datatype<Leaf<?>> refDatatype = (Datatype<Leaf<?>>) reference.getParameterDatatype();

                    List<Leaf<?>> values = selectByUuid(nextUuid, refDatatype.getPrimitive("parentUuid"), dateTime);
                    if (values.size() > 1) {
                        throw new VersioningException("to many rows with parentUuid = " + nextUuid);
                    }
                    values.forEach(leaf -> {
                        reference.set(entity, leaf);
                        refDatatype.getReference("parent").set(leaf, entity);
                        fetchGraph(leaf, dateTime);
                    });
                } else {
                    //Root
                    //todo
                }
            } else {
                //nonVersionable
                //todo
            }
        });
        datatype.getCollections().values().forEach(collection -> {
            if (collection.isVersionable()) {
                if (Leaf.class.isAssignableFrom(collection.getGraphType())) {
                    //Leaf
                    Datatype<Leaf<?>> colDatatype = (Datatype<Leaf<?>>) collection.getParameterDatatype();

                    List<Leaf<?>> values = selectByUuid(nextUuid, colDatatype.getPrimitive("parentUuid"), dateTime);

                    values.forEach(leaf -> {
                        collection.get(entity).add(leaf);
                        colDatatype.getReference("parent").set(leaf, entity);
                        fetchGraph(leaf, dateTime);
                    });
                } else {
                    //Root
                    //todo
                }
            } else {
                //nonVersionable
                //todo
            }
        });
    }

    private <T> List<T> selectByUuid(Serializable uuid, SingularParameter<T> uuidParameter, LocalDateTime dateTime) {
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
                cb.lessThan(root.get("lifetime").get("starting"), dateTime),
                cb.greaterThan(root.get("lifetime").get("expiring"), dateTime)
        );

        List<Tuple> tuples = em.createQuery(criteriaQuery).getResultList();

        return tuples.stream().map(tuple -> {
            T entity = datatype.instantiate();
            parameters.forEach(parameter -> parameter.set(
                    entity,
                    tuple.get(parameter.getName())
            ));
            return entity;
        }).collect(Collectors.toList());
    }
}
