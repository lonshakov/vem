package vem.util;

import io.persistence.vem.engine.impl.session.HibernateVersioningSessionFactory;
import io.persistence.vem.spi.session.VersioningEntityManagerFactory;
import org.hibernate.internal.SessionFactoryImpl;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class TestDatabase {
    private final static EntityManagerFactory emf = Persistence.createEntityManagerFactory("vem-test");
    private final static VersioningEntityManagerFactory vemf = new HibernateVersioningSessionFactory(emf.unwrap(SessionFactoryImpl.class));

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public VersioningEntityManagerFactory getVersioningEntityManagerFactory() {
        return vemf;
    }
}
