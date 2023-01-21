package lsa.prototype.vem.request;

public interface ChangeUnit<R extends ChangeRequest<?>> {
    R getRequest();

    void setRequest(R request);

    ChangeOperation getOperation();

    void setOperation(ChangeOperation operation);

    PolymorphEntity getLeaf();

    void setLeaf(PolymorphEntity leaf);
}
