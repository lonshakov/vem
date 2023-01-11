package lsa.prototype.vem.engine.impl.schema;

import jakarta.persistence.metamodel.Attribute;
import lsa.prototype.vem.engine.spi.schema.Datatype;
import lsa.prototype.vem.engine.spi.schema.Parameter;
import lsa.prototype.vem.model.basic.PersistedObject;
import org.hibernate.persister.entity.EntityPersister;

public class HibernateParameter<T extends PersistedObject> implements Parameter<T> {
    private final Datatype<T> structure;
    private final Attribute<? super T, ?> attribute;
    private final EntityPersister persister;

    public HibernateParameter(Datatype<T> structure, Attribute<? super T, ?> attribute, EntityPersister persister) {
        this.structure = structure;
        this.attribute = attribute;
        this.persister = persister;
    }

    @Override
    public String getName() {
        return attribute.getName();
    }

    @Override
    public Class<?> getType() {
        return attribute.getJavaType();
    }

    @Override
    public Datatype<?> getParameterDatatype() {
        return !isPrimitive()
                ? structure.getSchema().datatype((Class<? extends PersistedObject>) getType())
                : null;
    }

    @Override
    public Datatype<T> getStructureDatatype() {
        return structure;
    }

    @Override
    public boolean isCollection() {
        return attribute.isCollection();
    }

    @Override
    public boolean isReference() {
        return attribute.isAssociation();
    }

    @Override
    public void set(T owner, Object value) {
        persister.setPropertyValue(owner, getPropertyIndex(), value);
    }

    @Override
    public Object get(T owner) {
        return persister.getPropertyValue(owner, getName());
    }

    private int getPropertyIndex() {
        return persister.getEntityMetamodel().getPropertyIndex(getName());
    }
}
