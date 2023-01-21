package lsa.prototype.vem.model;

import java.io.Serializable;

public interface ILeafEntity<T extends IVersionedEntity> extends IVersionedEntity {
    T getParent();

    void setParent(T parent);

    Serializable getParentUuid();

    void setParentUuid(Serializable parentUuid);
}
