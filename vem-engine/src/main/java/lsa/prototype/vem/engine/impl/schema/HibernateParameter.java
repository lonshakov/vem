package lsa.prototype.vem.engine.impl.schema;

import jakarta.persistence.metamodel.Attribute;
import lsa.prototype.vem.model.basic.PersistedObject;
import lsa.prototype.vem.spi.schema.Accessor;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;
import org.hibernate.type.Type;

import java.io.Serializable;

public class HibernateParameter<T extends PersistedObject> implements Parameter<T> {
    private final Datatype<T> structure;
    private final Attribute<? super T, ?> attribute;
    private final Accessor accessor;
    private final Type hibernateType;

    public HibernateParameter(Datatype<T> structure, Attribute<? super T, ?> attribute, Accessor accessor, Type hibernateType) {
        this.structure = structure;
        this.attribute = attribute;
        this.accessor = accessor;
        this.hibernateType = hibernateType;
    }

    @Override
    public String getName() {
        return attribute.getName();
    }

    @Override
    public Class<?> getType() {
        return hibernateType.getReturnedClass();
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
    public void set(T owner, Serializable value) {
        accessor.set(owner, value);
    }

    @Override
    public Serializable get(T owner) {
        return accessor.get(owner);
    }
}
