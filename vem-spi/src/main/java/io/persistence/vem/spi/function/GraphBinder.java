package io.persistence.vem.spi.function;

import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeRequest;

public interface GraphBinder {
    <T extends Root> void bind(ChangeRequest<T> request);
}
