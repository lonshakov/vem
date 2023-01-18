package lsa.prototype.vem.engine.impl.schema;

import jakarta.persistence.metamodel.Attribute;
import lsa.prototype.vem.model.basic.PersistedObject;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;
import lsa.prototype.vem.spi.schema.Schema;
import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HibernateDatatype<T extends PersistedObject> implements Datatype<T> {
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
                    new Accessors.Parameter(entityPersister, name),
                    entityPersister.getPropertyType(name)
            );
            if (!attribute.isAssociation()) {
                primitives.put(name, parameter);
                continue;
            }
            if (PersistedObject.class.isAssignableFrom(attribute.getJavaType())) {
                references.put(name, parameter);
                continue;
            }
            collections.put(name, parameter);
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
}
