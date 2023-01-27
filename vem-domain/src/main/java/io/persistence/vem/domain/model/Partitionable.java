package io.persistence.vem.domain.model;

import java.io.Serializable;

public interface Partitionable {
    Serializable getPartition();
}
