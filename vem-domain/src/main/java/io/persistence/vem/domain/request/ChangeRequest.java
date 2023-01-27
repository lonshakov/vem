package io.persistence.vem.domain.request;

import io.persistence.vem.domain.model.Root;

import java.io.Serializable;

public interface ChangeRequest<T extends Root> extends Serializable {
    T getRoot();

    ChangeState getState();
}
