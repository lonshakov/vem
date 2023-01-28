package io.persistence.vem.spi.function;

import java.io.Serializable;
import java.time.LocalDateTime;

public interface Flashback {
    <T> T flashback(Class<T> type, Serializable uuid, LocalDateTime dateTime);
}
