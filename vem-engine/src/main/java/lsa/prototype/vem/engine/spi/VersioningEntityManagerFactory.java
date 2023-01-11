package lsa.prototype.vem.engine.spi;

import jakarta.persistence.EntityManagerFactory;
import lsa.prototype.vem.engine.spi.schema.HistoryMappings;
import lsa.prototype.vem.engine.spi.schema.Schema;

public interface VersioningEntityManagerFactory {
    VersioningEntityManager createEntityManager();

    Schema getSchema();

    HistoryMappings getHistoryMapping();

    EntityManagerFactory getJpaFactory();
}
