package lsa.prototype.vem.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class TestDatabase {
    private final static EntityManagerFactory emf = Persistence.createEntityManagerFactory("vem-test");

    public EntityManager newEntityManager() {
        return emf.createEntityManager();
    }
}
