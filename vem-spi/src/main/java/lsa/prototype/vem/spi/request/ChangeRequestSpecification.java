package lsa.prototype.vem.spi.request;

import lsa.prototype.vem.model.context.ChangeOperation;
import lsa.prototype.vem.model.version.LeafEntity;
import lsa.prototype.vem.model.version.RootEntity;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

public interface ChangeRequestSpecification<T extends RootEntity> extends Serializable {
    UUID getUuid();

    T getRoot();

    Set<Unit> getUnits();

    interface Unit extends Serializable {
        ChangeOperation getOperation();

        LeafEntity<?> getLeaf();
    }
}