package io.persistence.vem.domain.model;

public interface Versionable extends Partitionable {
    Version getVersion();

    Lifetime getLifetime();
}
