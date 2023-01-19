package lsa.prototype.vem.spi.session;

import jakarta.persistence.EntityManagerFactory;
import lsa.prototype.vem.spi.schema.HistoryMappings;
import lsa.prototype.vem.spi.schema.Schema;

public interface VersioningEntityManagerFactory {
    VersioningEntityManager createEntityManager();

    Schema getSchema();

    HistoryMappings getHistoryMapping();

    EntityManagerFactory getJpaFactory();
}
