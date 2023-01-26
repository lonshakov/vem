package lsa.prototype.vem.engine.impl.schema;

import javax.persistence.metamodel.EntityType;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Schema;
import org.hibernate.metamodel.spi.MetamodelImplementor;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class HibernateSchema implements Schema {
    private final ConcurrentHashMap<Class<?>, Datatype<?>> datatypes = new ConcurrentHashMap<>();

    public HibernateSchema(MetamodelImplementor metamodel) {
        for (EntityType<?> entityType : metamodel.getEntities()) {
            Class<?> type = entityType.getJavaType();
            Datatype<?> datatype = new HibernateDatatype(type, this, metamodel);
            datatypes.put(type, datatype);
        }
    }

    @Override
    public <T> Datatype<T> datatype(Class<T> type) {
        return (Datatype<T>) datatypes.get(type);
    }

    @Override
    public Iterator<Datatype<?>> iterator() {
        return datatypes.values().iterator();
    }
}
