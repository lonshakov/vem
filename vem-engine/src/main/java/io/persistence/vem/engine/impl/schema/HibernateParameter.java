package io.persistence.vem.engine.impl.schema;

import io.persistence.vem.spi.schema.*;
import org.hibernate.metamodel.model.domain.internal.AbstractPluralAttribute;
import org.hibernate.type.Type;

import javax.persistence.metamodel.Attribute;

abstract public class HibernateParameter<T> implements Parameter<T> {
    private final Datatype<T> structure;
    private final Attribute<T, ?> attribute;
    private final Accessor accessor;
    private final Type hibernateType;

    public HibernateParameter(Datatype<T> structure, Attribute<T, ?> attribute, Accessor accessor, Type hibernateType) {
        this.structure = structure;
        this.attribute = attribute;
        this.accessor = accessor;
        this.hibernateType = hibernateType;
    }

    @Override
    public Class<?> getJavaType() {
        //there's bug in hibernate attribute (only hibernateType works right)
        //don't change it
        return hibernateType.getReturnedClass();
    }

    /*only for entity types*/
    @Override
    public Class<?> getGraphType() {
        if (!attribute.isAssociation())
            return null;
        return attribute.isCollection()
                ? ((AbstractPluralAttribute) attribute).getValueGraphType().getJavaType()
                : getJavaType();
    }

    @Override
    public Attribute<T, ?> getAttribute() {
        return attribute;
    }

    @Override
    public Datatype<?> getParameterDatatype() {
        Class<?> type = getGraphType();
        return type == null
                ? null
                : structure.getSchema().getDatatype(type);
    }

    @Override
    public Datatype<T> getStructureDatatype() {
        return structure;
    }

    @Override
    public Accessor getAccessor() {
        return accessor;
    }

    public static class Singular<T> extends HibernateParameter<T> implements SingularParameter<T> {
        public Singular(Datatype<T> structure, Attribute<T, ?> attribute, Accessor accessor, Type hibernateType) {
            super(structure, attribute, accessor, hibernateType);
        }
    }

    public static class Plural<T> extends HibernateParameter<T> implements PluralParameter<T> {
        public Plural(Datatype<T> structure, Attribute<T, ?> attribute, Accessor accessor, Type hibernateType) {
            super(structure, attribute, accessor, hibernateType);
        }
    }
}
