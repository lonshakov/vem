package lsa.prototype.vem.engine.spi;

import lsa.prototype.vem.engine.spi.meta.HistoryMapping;
import lsa.prototype.vem.engine.spi.meta.Meta;

public interface VersioningEntityManagerFactory {
    VersioningEntityManager createEntityManager();
    Meta meta();

    HistoryMapping getHistoryMapping();
}
