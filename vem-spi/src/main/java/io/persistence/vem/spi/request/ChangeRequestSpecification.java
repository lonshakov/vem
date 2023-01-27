package io.persistence.vem.spi.request;

import io.persistence.vem.domain.model.Leaf;
import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeOperation;

import java.io.Serializable;
import java.util.List;

public interface ChangeRequestSpecification<T extends Root> extends Serializable {
    Serializable getUuid();

    T getRoot();

    List<Unit> getUnits();

    interface Unit extends Serializable {
        ChangeOperation getOperation();

        Leaf<?> getLeaf();
    }
}
