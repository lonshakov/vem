package io.persistence.vem.spi.request;

import io.persistence.vem.domain.model.GlobalEntity;
import io.persistence.vem.domain.model.Leaf;
import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeOperation;

import java.util.Set;

public interface ChangeRequestSpecification<T extends Root> extends GlobalEntity {
    T getRoot();

    Set<Unit> getUnits();

    interface Unit {
        ChangeOperation getOperation();

        Leaf<?> getLeaf();
    }
}
