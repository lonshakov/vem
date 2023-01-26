package io.persistence.vem.domain.request;

public interface ChangeUnit<R extends ChangeRequest<?>> {
    R getRequest();

    ChangeOperation getOperation();

    PolymorphEntity getLeaf();
}
