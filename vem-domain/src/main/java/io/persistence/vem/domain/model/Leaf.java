package io.persistence.vem.domain.model;

import java.io.Serializable;

public interface Leaf<T extends Versionable> extends Versionable {
    T getParent();

    Serializable getParentUuid();
}
