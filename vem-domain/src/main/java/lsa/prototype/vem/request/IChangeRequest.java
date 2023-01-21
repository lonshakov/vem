package lsa.prototype.vem.request;

import lsa.prototype.vem.model.IGlobalEntity;
import lsa.prototype.vem.model.IRootEntity;

public interface IChangeRequest<T extends IRootEntity> extends IGlobalEntity {
    T getRoot();

    void setRoot(T root);

    ChangeState getState();

    void setState(ChangeState state);
}
