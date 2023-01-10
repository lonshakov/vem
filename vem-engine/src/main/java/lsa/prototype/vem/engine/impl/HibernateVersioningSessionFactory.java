package lsa.prototype.vem.engine.impl;

import lsa.prototype.vem.engine.impl.meta.HibernateMeta;
import lsa.prototype.vem.engine.spi.VersioningEntityManager;
import lsa.prototype.vem.engine.spi.VersioningEntityManagerFactory;
import lsa.prototype.vem.engine.spi.meta.HistoryMapping;
import lsa.prototype.vem.engine.spi.meta.Meta;
import org.hibernate.internal.SessionFactoryImpl;

public class HibernateVersioningSessionFactory implements VersioningEntityManagerFactory {
    private final SessionFactoryImpl factory;
    private final Meta meta;
    private final HistoryMapping historyMapping;

    public HibernateVersioningSessionFactory(SessionFactoryImpl factory) {
        this.factory = factory;
        meta = new HibernateMeta(factory.getMetamodel());
        historyMapping = new HistoryMapping(meta);
    }

    @Override
    public VersioningEntityManager createEntityManager() {
        return new HibernateVersioningSession(this, factory.createEntityManager());
    }

    @Override
    public Meta meta() {
        return meta;
    }

    @Override
    public HistoryMapping getHistoryMapping() {
        return historyMapping;
    }
}
