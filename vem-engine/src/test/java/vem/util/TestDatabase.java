package vem.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lsa.prototype.vem.engine.impl.HibernateVersioningSessionFactory;
import lsa.prototype.vem.engine.spi.VersioningEntityManagerFactory;
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
