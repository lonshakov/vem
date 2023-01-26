package io.persistence.vem.spi.session;

import io.persistence.vem.spi.schema.HistoryMappings;
import io.persistence.vem.spi.schema.Schema;

import javax.persistence.EntityManagerFactory;

public interface VersioningEntityManagerFactory {
    VersioningEntityManager createEntityManager();

    Schema getSchema();

    HistoryMappings getHistoryMapping();

    EntityManagerFactory getJpaFactory();
}
