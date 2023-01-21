package lsa.prototype.vem.request;

import lsa.prototype.vem.model.IGlobalEntity;
import lsa.prototype.vem.model.ILeafEntity;
import lsa.prototype.vem.model.IRootEntity;

import java.util.Set;

public interface IChangeRequestSpecification<T extends IRootEntity> extends IGlobalEntity {
    T getRoot();

    Set<Unit> getUnits();

    interface Unit {
        ChangeOperation getOperation();

        ILeafEntity<?> getLeaf();
    }
}
