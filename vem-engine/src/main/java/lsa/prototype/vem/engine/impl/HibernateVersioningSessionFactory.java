package lsa.prototype.vem.engine.impl;

import jakarta.persistence.EntityManagerFactory;
import lsa.prototype.vem.engine.impl.schema.HibernateSchema;

import lsa.prototype.vem.spi.PersistenceProcessor;
import lsa.prototype.vem.spi.VersioningEntityManager;
import lsa.prototype.vem.spi.VersioningEntityManagerFactory;
import lsa.prototype.vem.spi.schema.HistoryMappings;
import lsa.prototype.vem.spi.schema.Schema;
import org.hibernate.internal.SessionFactoryImpl;

import java.util.concurrent.ConcurrentHashMap;

public class HibernateVersioningSessionFactory implements VersioningEntityManagerFactory {
    private final SessionFactoryImpl factory;
    private final Schema schema;
    private final HistoryMappings historyMappings;
    private final ConcurrentHashMap<String, PersistenceProcessor> processors = new ConcurrentHashMap<>();

    public HibernateVersioningSessionFactory(SessionFactoryImpl factory) {
        this.factory = factory;
        schema = new HibernateSchema(factory.getMetamodel());
        historyMappings = new HistoryMappings(schema);

        //default values
        processors.put("recursive-persist", new Persister());
        processors.put("recursive-merge", new Persister());
    }

    @Override
    public VersioningEntityManager createEntityManager() {
        return new HibernateVersioningSession(
                this,
                factory.createEntityManager(),
                processors
        );
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public HistoryMappings getHistoryMapping() {
        return historyMappings;
    }

    @Override
    public EntityManagerFactory getJpaFactory() {
        return factory;
    }
}
