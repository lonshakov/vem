package io.persistence.vem.domain.model;

import java.io.Serializable;

public interface Partitionable extends GlobalEntity {
    Serializable getPartition();
}
