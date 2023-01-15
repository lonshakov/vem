package lsa.prototype.vem.engine.impl.schema;

import jakarta.persistence.metamodel.Attribute;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;
import lsa.prototype.vem.spi.schema.Schema;
import lsa.prototype.vem.model.basic.PersistedObject;
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
    private final EntityPersister persister;
    private final EntityTypeDescriptor<T> descriptor;

    public HibernateDatatype(Class<T> type, HibernateSchema schema) {
        this.schema = schema;

        MetamodelImplementor hibernateMetamodel = schema.getHibernateMetamodel();
        descriptor = hibernateMetamodel.entity(type);
        persister = hibernateMetamodel.entityPersister(type);

        for (Attribute<? super T, ?> attribute : descriptor.getAttributes()) {
            String name = attribute.getName();
            Parameter<T> parameter = new HibernateParameter<>(this, attribute, persister);

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

        identifier = primitives.get("id");

    }

    @Override
    public T instantiate() {
        return (T) persister.getEntityTuplizer().instantiate();
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
        return descriptor.getJavaType();
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

}
