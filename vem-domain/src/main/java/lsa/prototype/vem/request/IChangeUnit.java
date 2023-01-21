package lsa.prototype.vem.request;

public interface IChangeUnit<R extends IChangeRequest<?>> {
    R getRequest();

    void setRequest(R request);

    ChangeOperation getOperation();

    void setOperation(ChangeOperation operation);

    PolymorphEntity getLeaf();

    void setLeaf(PolymorphEntity leaf);
}
