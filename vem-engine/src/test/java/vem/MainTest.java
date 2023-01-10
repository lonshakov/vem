package vem;

import jakarta.persistence.EntityManager;
import lsa.prototype.vem.engine.impl.HibernateVersioningSessionFactory;
import lsa.prototype.vem.engine.impl.meta.HibernateMeta;
import lsa.prototype.vem.engine.spi.VersioningEntityManager;
import lsa.prototype.vem.engine.spi.VersioningEntityManagerFactory;
import lsa.prototype.vem.engine.spi.meta.HistoryMapping;
import lsa.prototype.vem.engine.spi.meta.Meta;
import lsa.prototype.vem.model.version.Leaf;
import lsa.prototype.vem.model.version.VersionedEntity;
import org.hibernate.Session;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.internal.SessionImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vem.context.StoreChangeRequest;
import vem.context.StoreChangeUnit;
import vem.entity.Item;
import vem.entity.Parcel;
import vem.entity.Store;
import vem.util.TestDatabase;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MainTest {
    private final TestDatabase database = new TestDatabase();
    private final BiConsumer<Consumer<EntityManager>, Consumer<EntityManager>> tester = (conditions, checks) -> {
        try (EntityManager em = database.newEntityManager()) {
            em.unwrap(Session.class).enableFilter("CurrentVersion");
            em.getTransaction().begin();
            conditions.accept(em);
            em.getTransaction().commit();
            em.clear();
            checks.accept(em);
        }
    };

    @Test
    void testVisibility() {
        tester.accept(
                (em) -> {
                    Store store = new Store();
                    store.setName("x5");


                    Parcel parcel1 = new Parcel();
                    parcel1.setVersionState(VersionedEntity.State.ACTIVE);
                    parcel1.setParent(store);
                    em.persist(parcel1);

                    Parcel parcel2 = new Parcel();
                    parcel2.setVersionState(VersionedEntity.State.PASSIVE);
                    parcel2.setParent(store);
                    em.persist(parcel2);

                    em.persist(store);
                },
                (em) -> Assertions.assertEquals(
                        1,
                        em.createQuery("select s from Store s where s.name = 'x5'", Store.class)
                                .getSingleResult()
                                .getParcels()
                                .size()
                )
        );
    }

    @Test
    void testChangeRequest() {
        tester.accept(
                (em) -> {
                    Store store = new Store();
                    store.setName("ozon");
                    em.persist(store);

                    Parcel parcel = new Parcel();
                    parcel.setName("notebook");
                    parcel.setParent(store);
                    em.persist(parcel);

                    StoreChangeRequest request = new StoreChangeRequest();
                    request.setRoot(store);
                    em.persist(request);

                    StoreChangeUnit unit = new StoreChangeUnit();
                    unit.setRequest(request);
                    unit.setLeaf(parcel);
                    em.persist(unit);
                },
                (em) -> {
                    StoreChangeRequest queriedRequest = em.createQuery(
                                    "select r from StoreChangeRequest r where r.root.name = 'ozon'",
                                    StoreChangeRequest.class)
                            .getSingleResult();
                    Leaf<?> queriedLeaf = queriedRequest.getUnits().stream().findFirst().get().getLeaf();

                    Assertions.assertEquals("ozon", queriedRequest.getRoot().getName());
                    Assertions.assertEquals("notebook", ((Parcel) queriedLeaf).getName());
                }
        );
    }

    @Test
    void testVersionedPersist() {
        VersioningEntityManagerFactory factory = new HibernateVersioningSessionFactory(
                database.getEntityManagerFactory().unwrap(SessionFactoryImpl.class)
        );

        try (VersioningEntityManager vem = factory.createEntityManager()) {

            Store store = new Store();
            store.setName("Macy's");

            Parcel parcel = new Parcel();
            parcel.setName("Present");
            store.getParcels().add(parcel);

            Item item = new Item();
            item.setName("Converse");
            parcel.getItems().add(item);

            vem.em().getTransaction().begin();

            vem.persist(store);

            vem.em().getTransaction().commit();
            vem.em().clear();

            System.out.println();
        }

        try (EntityManager em = database.newEntityManager()) {
            StoreChangeRequest request = em.createQuery(
                    "select c from StoreChangeRequest c where c.root.name = 'Macy''s'",
                    StoreChangeRequest.class
            ).getSingleResult();

            Assertions.assertEquals(2, request.getUnits().size());
        }
    }

    @Test
    void testMetadataCreation() {
        try (EntityManager em = database.newEntityManager()) {
            Meta meta = new HibernateMeta(
                    em.unwrap(SessionImpl.class).getMetamodel()
            );
            HistoryMapping historyMapping = new HistoryMapping(meta);
        }
    }
}
