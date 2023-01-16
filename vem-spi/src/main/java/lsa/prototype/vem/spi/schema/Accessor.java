package lsa.prototype.vem.spi.schema;

import lsa.prototype.vem.model.basic.PersistedObject;

import java.io.Serializable;

public interface Accessor {
    void set(PersistedObject entity, Serializable value);

    Serializable get(PersistedObject entity);
}
