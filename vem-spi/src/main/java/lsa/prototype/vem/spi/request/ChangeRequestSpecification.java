package lsa.prototype.vem.spi.request;

import lsa.prototype.vem.model.GlobalEntity;
import lsa.prototype.vem.model.Leaf;
import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.request.ChangeOperation;

import java.util.Set;

public interface ChangeRequestSpecification<T extends Root> extends GlobalEntity {
    T getRoot();

    Set<Unit> getUnits();

    interface Unit {
        ChangeOperation getOperation();

        Leaf<?> getLeaf();
    }
}
