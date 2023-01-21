package lsa.prototype.vem.model;

import java.io.Serializable;

public interface IGlobalEntity extends IPersistedObject, Serializable {
    Serializable getUuid();

    void setUuid(Serializable uuid);
}
