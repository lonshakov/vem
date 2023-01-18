package lsa.prototype.vem.spi.schema;

import lsa.prototype.vem.model.basic.PersistedObject;

public interface Accessor {
    void set(PersistedObject entity, Object value);

    Object get(PersistedObject entity);
}
