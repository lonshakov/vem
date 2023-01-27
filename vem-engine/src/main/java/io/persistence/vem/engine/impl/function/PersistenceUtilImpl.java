package io.persistence.vem.engine.impl.function;

import io.persistence.vem.spi.function.PersistenceUtil;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.Schema;

import java.io.Serializable;

public class PersistenceUtilImpl implements PersistenceUtil {
    private final Schema schema;

    public PersistenceUtilImpl(Schema schema) {
        this.schema = schema;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public Serializable getId(Object obj) {
        if (obj == null)
            return null;
        Datatype datatype = getSchema().getDatatype(obj);
        if (datatype == null)
            return null;
        return (Serializable) datatype.getIdentifier().get(obj);
    }

    @Override
    public Serializable getUuid(Object obj) {
        if (obj == null)
            return null;
        Datatype datatype = getSchema().getDatatype(obj);
        if (datatype == null || !datatype.isGlobal())
            return null;
        return (Serializable) datatype.getGlobalIdentifier().get(obj);
    }
}
