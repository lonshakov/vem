package vem.util;

import io.persistence.vem.engine.impl.session.HibernateVersioningSessionFactory;
import io.persistence.vem.spi.session.VersioningEntityManagerFactory;
import org.hibernate.internal.SessionFactoryImpl;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class TestDatabase {
    private final static VersioningEntityManagerFactory vemf = new HibernateVersioningSessionFactory(
            Persistence.createEntityManagerFactory("vem-test").unwrap(SessionFactoryImpl.class),
            new SessionContextServiceStab()
    );

    public VersioningEntityManagerFactory getVersioningEntityManagerFactory() {
        return vemf;
    }
}
