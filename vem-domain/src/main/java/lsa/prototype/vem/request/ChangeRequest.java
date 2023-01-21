package lsa.prototype.vem.request;

import lsa.prototype.vem.model.GlobalEntity;
import lsa.prototype.vem.model.Root;

public interface ChangeRequest<T extends Root> extends GlobalEntity {
    T getRoot();

    void setRoot(T root);

    ChangeState getState();

    void setState(ChangeState state);
}
