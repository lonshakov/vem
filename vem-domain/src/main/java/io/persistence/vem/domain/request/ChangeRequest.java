package io.persistence.vem.domain.request;

import io.persistence.vem.domain.model.GlobalEntity;
import io.persistence.vem.domain.model.Root;

public interface ChangeRequest<T extends Root> extends GlobalEntity {
    T getRoot();

    ChangeState getState();
}
