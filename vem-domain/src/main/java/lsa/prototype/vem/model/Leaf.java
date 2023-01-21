package lsa.prototype.vem.model;

import java.io.Serializable;

public interface Leaf<T extends Versionable> extends Versionable {
    T getParent();

    void setParent(T parent);

    Serializable getParentUuid();

    void setParentUuid(Serializable parentUuid);
}
