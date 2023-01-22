package lsa.prototype.vem.model;

import java.io.Serializable;

public interface Leaf<T extends Versionable> extends Versionable {
    T getParent();

    Serializable getParentUuid();
}
