package io.persistence.vem.engine.impl.session;

import io.persistence.vem.engine.impl.function.Persister;
import io.persistence.vem.engine.impl.schema.HibernateSchema;
import io.persistence.vem.spi.function.PersistenceProcessor;
import io.persistence.vem.spi.schema.HistoryMappings;
import io.persistence.vem.spi.schema.Schema;
import io.persistence.vem.spi.session.VersioningEntityManager;
import io.persistence.vem.spi.session.VersioningEntityManagerFactory;
import org.hibernate.internal.SessionFactoryImpl;

import javax.persistence.EntityManagerFactory;
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
        processors.put("entity-persist", new Persister());
        processors.put("entity-merge", new Persister());
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
