package vem.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lsa.prototype.vem.engine.impl.session.HibernateVersioningSessionFactory;
import lsa.prototype.vem.spi.session.VersioningEntityManagerFactory;
import org.hibernate.internal.SessionFactoryImpl;

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
