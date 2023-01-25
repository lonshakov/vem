package lsa.prototype.vem.model;

import java.io.Serializable;

public interface Partitionable extends GlobalEntity {
    Serializable getPartition();
}
