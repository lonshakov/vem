package io.persistence.vem.engine.impl.schema;

import io.persistence.vem.domain.model.GlobalId;
import io.persistence.vem.engine.impl.function.Cloner;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.PluralParameter;
import io.persistence.vem.spi.schema.Schema;
import io.persistence.vem.spi.schema.SingularParameter;
import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;

import javax.persistence.Id;
import javax.persistence.Version;
import javax.persistence.metamodel.Attribute;
import java.lang.reflect.AccessibleObject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HibernateDatatype<T> implements Datatype<T> {
    private final HibernateSchema schema;
    private final ConcurrentHashMap<String, SingularParameter<T>> primitives = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SingularParameter<T>> references = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PluralParameter<T>> collections = new ConcurrentHashMap<>();
    private final SingularParameter<T> identifier;
    private final SingularParameter<T> globalIdentifier;
    private final EntityPersister entityPersister;
    private final EntityTypeDescriptor<T> entityDescriptor;

    public HibernateDatatype(Class<T> type, HibernateSchema schema, MetamodelImplementor metamodel) {
        this.schema = schema;

        entityDescriptor = metamodel.entity(type);
        entityPersister = metamodel.entityPersister(type);

        SingularParameter<T> globalIdentifier = null;

        for (Attribute<? super T, ?> attribute : entityDescriptor.getAttributes()) {
            String name = attribute.getName();
            AccessibleObject member = (AccessibleObject) attribute.getJavaMember();
            if (Set.of(Id.class, Version.class).stream().anyMatch(member::isAnnotationPresent)) {
                continue;
            }

            if (attribute.isCollection()) {
                PluralParameter<T> parameter = new HibernateParameter.Plural<>(
                        this,
                        (Attribute<T, ?>) attribute,
                        new Accessors.Primitive(entityPersister, name),
                        entityPersister.getPropertyType(name)
                );
                collections.put(name, parameter);
            } else {
                SingularParameter<T> parameter = new HibernateParameter.Singular<>(
                        this,
                        (Attribute<T, ?>) attribute,
                        new Accessors.Primitive(entityPersister, name),
                        entityPersister.getPropertyType(name)
                );
                if (attribute.isAssociation()) {
                    references.put(name, parameter);
                } else if (member.isAnnotationPresent(GlobalId.class)) {
                    globalIdentifier = parameter;
                } else {
                    primitives.put(name, parameter);
                }
            }
        }

        this.globalIdentifier = globalIdentifier;

        identifier = new HibernateParameter.Singular<>(
                this,
                (Attribute<T, ?>) entityDescriptor.getId(entityDescriptor.getIdType().getJavaType()),
                new Accessors.Id(entityPersister),
                entityPersister.getIdentifierType()
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
    public SingularParameter<T> getIdentifier() {
        return identifier;
    }

    @Override
    public SingularParameter<T> getGlobalIdentifier() {
        return globalIdentifier;
    }

    @Override
    public SingularParameter<T> getPrimitive(String name) {
        return primitives.get(name);
    }

    @Override
    public SingularParameter<T> getReference(String name) {
        return references.get(name);
    }

    @Override
    public PluralParameter<T> getCollection(String name) {
        return collections.get(name);
    }

    @Override
    public Map<String, SingularParameter<T>> getPrimitives() {
        return Collections.unmodifiableMap(primitives);
    }

    @Override
    public Map<String, SingularParameter<T>> getReferences() {
        return Collections.unmodifiableMap(references);
    }

    @Override
    public Map<String, PluralParameter<T>> getCollections() {
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
