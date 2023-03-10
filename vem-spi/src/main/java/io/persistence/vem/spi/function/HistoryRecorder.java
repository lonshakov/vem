package io.persistence.vem.spi.function;

import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeRequest;

public interface HistoryRecorder {
    <T extends Root> void record(ChangeRequest<T> request);
}
