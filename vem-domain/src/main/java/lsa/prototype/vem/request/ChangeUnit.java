package lsa.prototype.vem.request;

public interface ChangeUnit<R extends ChangeRequest<?>> {
    R getRequest();

    ChangeOperation getOperation();

    PolymorphEntity getLeaf();
}
