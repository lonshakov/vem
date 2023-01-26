package io.persistence.vem.engine.impl.schema;

import io.persistence.vem.engine.impl.function.Cloner;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.Parameter;
import io.persistence.vem.spi.schema.Schema;
import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;

import javax.persistence.metamodel.Attribute;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HibernateDatatype<T> implements Datatype<T> {
    private final HibernateSchema schema;
    private final ConcurrentHashMap<String, Parameter<T>> primitives = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Parameter<T>> references = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Parameter<T>> collections = new ConcurrentHashMap<>();
    private final Parameter<T> identifier;
    private final EntityPersister entityPersister;
    private final EntityTypeDescriptor<T> entityDescriptor;

    public HibernateDatatype(Class<T> type, HibernateSchema schema, MetamodelImplementor metamodel) {
        this.schema = schema;

        entityDescriptor = metamodel.entity(type);
        entityPersister = metamodel.entityPersister(type);

        for (Attribute<? super T, ?> attribute : entityDescriptor.getAttributes()) {
            String name = attribute.getName();
            if ("id".equals(name)) {
                continue;
            }
            Parameter<T> parameter = new HibernateParameter<>(
                    this,
                    attribute,
                    new Accessors.Primitive(entityPersister, name),
                    entityPersister.getPropertyType(name)
            );
            if (attribute.isAssociation()) {
                if (attribute.isCollection()) {
                    collections.put(name, parameter);
                } else {
                    references.put(name, parameter);
                }
            } else {
                primitives.put(name, parameter);
            }
        }

        identifier = new HibernateParameter<>(
                this,
                entityDescriptor.getAttribute("id"),
                new Accessors.Id(entityPersister),
                entityPersister.getPropertyType("id")
        );
    }

    @Override
    public T instantiate() {
        return (T) entityPersister.getEntityTuplizer().instantiate();
    }

    @Override
    public T clone(T entity) {
        return new Cloner().clone(entity, getSchema());
    }

    @Override
    public Parameter<T> identifier() {
        return identifier;
    }

    @Override
    public Parameter<T> primitive(String name) {
        return primitives.get(name);
    }

    @Override
    public Parameter<T> reference(String name) {
        return references.get(name);
    }

    @Override
    public Parameter<T> collection(String name) {
        return collections.get(name);
    }

    @Override
    public Map<String, Parameter<T>> primitives() {
        return Collections.unmodifiableMap(primitives);
    }

    @Override
    public Map<String, Parameter<T>> references() {
        return Collections.unmodifiableMap(references);
    }

    @Override
    public Map<String, Parameter<T>> collections() {
        return Collections.unmodifiableMap(collections);
    }

    @Override
    public Class<T> getJavaType() {
        return entityDescriptor.getJavaType();
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public String toString() {
        return entityDescriptor.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HibernateDatatype<?> that = (HibernateDatatype<?>) o;
        return entityDescriptor.equals(that.entityDescriptor);
    }

    @Override
    public int hashCode() {
        return entityDescriptor.hashCode();
    }
}
