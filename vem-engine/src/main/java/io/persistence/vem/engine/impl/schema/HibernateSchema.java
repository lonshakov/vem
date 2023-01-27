package io.persistence.vem.engine.impl.schema;

import io.persistence.vem.engine.impl.function.PersistenceUtilImpl;
import io.persistence.vem.spi.function.PersistenceUtil;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.Schema;
import org.hibernate.metamodel.spi.MetamodelImplementor;

import javax.persistence.metamodel.EntityType;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class HibernateSchema implements Schema {
    private final ConcurrentHashMap<Class<?>, Datatype<?>> datatypes = new ConcurrentHashMap<>();
    private final PersistenceUtil util;

    public HibernateSchema(MetamodelImplementor metamodel) {
        for (EntityType<?> entityType : metamodel.getEntities()) {
            Class<?> type = entityType.getJavaType();
            Datatype<?> datatype = new HibernateDatatype(type, this, metamodel);
            datatypes.put(type, datatype);
        }
        util = new PersistenceUtilImpl(this);
    }

    @Override
    public <T> Datatype<T> getDatatype(Class<T> type) {
        return (Datatype<T>) datatypes.get(type);
    }

    @Override
    public PersistenceUtil getUtil() {
        return util;
    }

    @Override
    public Iterator<Datatype<?>> iterator() {
        return datatypes.values().iterator();
    }
}
