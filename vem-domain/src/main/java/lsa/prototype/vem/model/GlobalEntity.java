package lsa.prototype.vem.model;

import java.io.Serializable;

public interface GlobalEntity extends Persistable, Serializable {
    Serializable getUuid();

    void setUuid(Serializable uuid);
}
