package lsa.prototype.vem.engine.impl.schema;

import jakarta.persistence.metamodel.EntityType;
import lsa.prototype.vem.model.basic.PersistedObject;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Schema;
import org.hibernate.metamodel.spi.MetamodelImplementor;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class HibernateSchema implements Schema {
    private final ConcurrentHashMap<Class<?>, Datatype<?>> datatypes = new ConcurrentHashMap<>();
    private final MetamodelImplementor metamodel;

    public HibernateSchema(MetamodelImplementor metamodel) {
        this.metamodel = metamodel;
        for (EntityType<?> entityType : metamodel.getEntities()) {
            Class<?> type = entityType.getJavaType();
            Datatype<?> datatype = new HibernateDatatype(type, this);
            datatypes.put(type, datatype);
        }
    }

    @Override
    public <T extends PersistedObject> Datatype<T> datatype(Class<T> type) {
        return (Datatype<T>) datatypes.get(type);
    }

    protected MetamodelImplementor getHibernateMetamodel() {
        return metamodel;
    }

    @Override
    public Iterator<Datatype<?>> iterator() {
        return datatypes.values().iterator();
    }
}
