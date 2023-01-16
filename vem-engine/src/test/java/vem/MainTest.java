package vem;

import lsa.prototype.vem.engine.impl.schema.HibernateSchema;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.version.EntityVersion;
import lsa.prototype.vem.model.version.Leaf;
import lsa.prototype.vem.spi.VersioningEntityManager;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.HistoryMappings;
import lsa.prototype.vem.spi.schema.Schema;
import org.hibernate.Session;
import org.hibernate.internal.SessionFactoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vem.context.StoreChangeRequest;
import vem.context.StoreChangeUnit;
import vem.entity.Item;
import vem.entity.Parcel;
import vem.entity.Store;
import vem.util.TestDatabase;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MainTest {
    private final TestDatabase database = new TestDatabase();
    private final BiConsumer<Consumer<VersioningEntityManager>, Consumer<VersioningEntityManager>> tester = (conditions, checks) -> {
        try (VersioningEntityManager vem = database.getVersioningEntityManagerFactory().createEntityManager()) {
            //define
            Session session = vem.em().unwrap(Session.class);
            session.enableFilter("CurrentVersion");
            //prepare
            session.getTransaction().begin();
            conditions.accept(vem);
            session.getTransaction().commit();
            //clean
            session.clear();
            session.getSessionFactory().getCache().evictAll();
            //check
            session.getTransaction().begin();
            checks.accept(vem);
            session.getTransaction().commit();
        }
    };

    @Test
    void testVisibility() {
        tester.accept(
                (vem) -> {
                    Store store = new Store();
                    store.setName("x5");

                    Parcel parcel1 = new Parcel();
                    parcel1.getVersion().setStateType(EntityVersion.StateType.ACTIVE);
                    parcel1.setParent(store);
                    vem.em().persist(parcel1);

                    Parcel parcel2 = new Parcel();
                    parcel2.getVersion().setStateType(EntityVersion.StateType.PASSIVE);
                    parcel2.setParent(store);
                    vem.em().persist(parcel2);

                    vem.em().persist(store);
                },
                (vem) -> {
                    Store store = vem.em().createQuery("select s from Store s where s.name = 'x5'", Store.class)
                            .getSingleResult();

                    Assertions.assertEquals(
                            1,
                            store.getParcels()
                                    .size()
                    );

                    Assertions.assertEquals(
                            2,
                            vem.em().createQuery("select p from Parcel p where p.parent = :parent", Parcel.class)
                                    .setParameter("parent", store)
                                    .getResultList()
                                    .size()
                    );
                }
        );
    }

    @Test
    void testChangeRequest() {
        tester.accept(
                (vem) -> {
                    Store store = new Store();
                    store.setName("ozon");
                    vem.em().persist(store);

                    Parcel parcel = new Parcel();
                    parcel.setName("notebook");
                    parcel.setParent(store);
                    vem.em().persist(parcel);

                    StoreChangeRequest request = new StoreChangeRequest();
                    request.setRoot(store);
                    vem.em().persist(request);

                    StoreChangeUnit unit = new StoreChangeUnit();
                    unit.setRequest(request);
                    unit.setLeaf(parcel);
                    vem.em().persist(unit);
                },
                (vem) -> {
                    StoreChangeRequest queriedRequest = vem.em().createQuery(
                                    "select r from StoreChangeRequest r where r.root.name = 'ozon'",
                                    StoreChangeRequest.class)
                            .getSingleResult();

                    Leaf<?> queriedLeaf = vem.getChanger().stream(queriedRequest).findFirst().get();

                    Assertions.assertEquals("ozon", queriedRequest.getRoot().getName());
                    Assertions.assertEquals("notebook", ((Parcel) queriedLeaf).getName());
                }
        );
    }

    @Test
    void testVersionedPersist() {
        tester.accept(
                (vem) -> {
                    Store store = new Store();
                    store.setName("Macy's");

                    Parcel parcel = new Parcel();
                    parcel.setName("Present");
                    store.getParcels().add(parcel);

                    Item item = new Item();
                    item.setName("Converse");
                    parcel.getItems().add(item);

                    ChangeRequest<Store> request = vem.persist(store);

                    vem.publish(request);

                    vem.affirm(request);
                },
                (vem) -> {
                    StoreChangeRequest request = vem.em().createQuery(
                            "select c from StoreChangeRequest c where c.root.name = 'Macy''s'",
                            StoreChangeRequest.class
                    ).getSingleResult();

                    Assertions.assertEquals(
                            2,
                            vem.getChanger()
                                    .stream(request)
                                    .count()
                    );
                }
        );
    }

    @Test
    void testMetadataCreation() {
        Schema schema = new HibernateSchema(database.getEntityManagerFactory().unwrap(SessionFactoryImpl.class).getMetamodel());

        Datatype<Store> datatype = schema.datatype(Store.class);

        Store store = new Store();

        Parcel parcel = new Parcel();
        parcel.setName("sweets");

        datatype.primitive("name").set(store, "MVideo");
        datatype.identifier().set(store, 100L);
        ((List<Parcel>) datatype.collection("parcels").get(store)).add(parcel);

        Assertions.assertEquals("MVideo", store.getName());
        Assertions.assertEquals(100L, store.getId());
        Assertions.assertEquals("sweets", store.getParcels().get(0).getName());

        HistoryMappings historyMappings = new HistoryMappings(schema);

        Assertions.assertEquals(historyMappings.get(Store.class), historyMappings.get(Parcel.class));
    }
}
