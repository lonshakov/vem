package vem;

import jakarta.persistence.EntityManager;
import lsa.prototype.vem.engine.impl.request.CRSpecificationDTO;
import lsa.prototype.vem.engine.impl.request.CRUnitDTO;
import lsa.prototype.vem.model.Leaf;
import lsa.prototype.vem.model.context.ChangeRequestTemplate;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.request.ChangeRequest;
import lsa.prototype.vem.spi.request.ChangeRequestSpecification;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.HistoryMappings;
import lsa.prototype.vem.spi.schema.Schema;
import lsa.prototype.vem.spi.session.VersioningEntityManager;
import org.hibernate.internal.SessionImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vem.context.StoreChangeRequest;
import vem.context.StoreChangeUnit;
import vem.entity.Item;
import vem.entity.Parcel;
import vem.entity.Store;
import vem.entity.StoreBody;
import vem.util.TestDatabase;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class MainTest {
    private final TestDatabase database = new TestDatabase();
    private final Consumer<Consumer<VersioningEntityManager>> isolator = (action) -> {
        try (VersioningEntityManager vem = database.getVersioningEntityManagerFactory().createEntityManager()) {
            EntityManager em = vem.em();
            //prepare
            em.getTransaction().begin();
            action.accept(vem);
            em.getTransaction().commit();
            //clean
            em.clear();
            em.getEntityManagerFactory().getCache().evictAll();
        }
    };

    @Test
    void testPersist() {
        isolator.accept((vem) -> {
            Store store = new Store("Macy's");
            store.setBody(new StoreBody("Moscow"));

            Parcel parcel = new Parcel("Present");
            store.getParcels().add(parcel);

            Item item = new Item("Converse");
            parcel.getItems().add(item);

            ChangeRequest<Store> request = vem.persist(store);
            vem.publish(request);
            vem.affirm(request);
            System.out.println();
        });
        isolator.accept((vem) -> {
            StoreChangeRequest request = vem.em()
                    .createQuery("select c from StoreChangeRequest c where c.root.name = 'Macy''s'", StoreChangeRequest.class)
                    .getSingleResult();

            Assertions.assertEquals(3, vem.getChanger().stream(request, true).count());

            Store store = vem.em()
                    .createQuery("select s from Store s where s.name = 'Macy''s'", Store.class)
                    .getSingleResult();

            Assertions.assertEquals(1, store.getParcels().size());

            Assertions.assertEquals(1, store.getParcels().get(0).getItems().size());

            Assertions.assertNotNull(store.getBody());
        });
    }

    @Test
    void testMerge() {
        Function<VersioningEntityManager, Store> selectX5 = vem -> vem.em()
                .createQuery("select s from Store s where s.name = 'x5'", Store.class)
                .getSingleResult();

        //persist (initial)
        isolator.accept((vem) -> {
            Store store = new Store("x5");
            store.setBody(new StoreBody("Bali"));

            Parcel parcel = new Parcel("box1");
            parcel.getItems().add(new Item("item1"));

            store.getParcels().add(parcel);

            ChangeRequest<Store> request = vem.persist(store);
            vem.publish(request);
            vem.affirm(request);

            Assertions.assertEquals(3, vem.getChanger().stream(request, true).count());
        });
        isolator.accept((vem) -> {
            Store store = selectX5.apply(vem);

            Assertions.assertEquals(1, store.getParcels().size());

            Assertions.assertEquals(1, store.getParcels().get(0).getItems().size());
        });

        //merge (add leaf leve 1)
        isolator.accept((vem) -> {
            Store store = selectX5.apply(vem);

            store.getParcels().add(new Parcel("box2"));

            ChangeRequest<Store> request = vem.merge(store);
            vem.publish(request);
            vem.affirm(request);

            Assertions.assertEquals(1, vem.getChanger().stream(request, true).count());
        });
        isolator.accept((vem) -> {
            Store store = selectX5.apply(vem);

            Assertions.assertEquals(2, store.getParcels().size());
        });

        //merge (remove leaf level 1)
        isolator.accept((vem) -> {
            Store store = selectX5.apply(vem);
            Parcel box2 = store.getParcels().stream().filter(p -> p.getName().equals("box2")).findFirst().get();

            store.getParcels().remove(box2);
            //setVersion(vem, box2, new Version(VersionState.PURGE, 0));

            ChangeRequest<Store> request = vem.merge(store);
            vem.publish(request);
            vem.affirm(request);

            Assertions.assertEquals(1, vem.getChanger().stream(request, true).count());
        });
        isolator.accept((vem) -> {
            Store store = selectX5.apply(vem);

            Assertions.assertEquals(1, store.getParcels().size());

            List<Parcel> parcels = vem.em().createQuery("select p from Parcel p where p.parentUuid = :parentUuid", Parcel.class)
                    .setParameter("parentUuid", store.getUuid())
                    .getResultList();

            Assertions.assertEquals(2, parcels.size());
        });

        //merge (add leaf leve 2)
        isolator.accept((vem) -> {
            Store store = selectX5.apply(vem);
            store.getParcels().get(0).getItems().add(new Item("item2"));

            ChangeRequest<Store> request = vem.merge(store);
            vem.publish(request);
            vem.affirm(request);

            Assertions.assertEquals(1, vem.getChanger().stream(request, true).count());
        });
        isolator.accept((vem) -> {
            Store store = selectX5.apply(vem);

            Assertions.assertEquals(2, store.getParcels().get(0).getItems().size());
        });

        //merge (remove leaf level 2)
        isolator.accept((vem) -> {
            Store store = selectX5.apply(vem);
            Parcel parcel = store.getParcels().get(0);

            Item item = parcel.getItems().stream().filter(o -> o.getName().equals("item2")).findFirst().get();

            //try to remove an item
            parcel.getItems().remove(item);

            ChangeRequest<Store> request = vem.merge(store);
            vem.publish(request);
            vem.affirm(request);

            Assertions.assertEquals(1, vem.getChanger().stream(request, true).count());
        });
        isolator.accept((vem) -> {
            Store store = selectX5.apply(vem);
            Parcel parcel = store.getParcels().get(0);

            Assertions.assertEquals(1, parcel.getItems().size());

            List<Item> items = vem.em().createQuery("select i from Item i where i.parentUuid = :parentUuid", Item.class)
                    .setParameter("parentUuid", parcel.getUuid())
                    .getResultList();

            Assertions.assertEquals(2, items.size());
        });
        //merge (update body)
        isolator.accept((vem) -> {
            SessionImpl session = vem.em().unwrap(SessionImpl.class);

            Store store = selectX5.apply(vem);
            StoreBody body = store.getBody();

            store.setBody(new StoreBody("Phuket"));

            //vem.em().detach(store);
            //session.contains();

            ChangeRequest<Store> request = vem.merge(store);
            vem.publish(request);
            vem.affirm(request);

            Assertions.assertEquals(1, vem.getChanger().stream(request, true).count());
        });
    }

    @Test
    void testChangeRequestSpecification() {
        isolator.accept((vem) -> {
            Store store = new Store("drugs");

            StoreBody body = new StoreBody("One Pickwick Plaza");
            body.setParentUuid(store.getUuid());

            Parcel parcel = new Parcel("patches");
            parcel.setParentUuid(store.getUuid());

            ChangeRequestSpecification<Store> crs = new CRSpecificationDTO<>(store);
            crs.getUnits().add(new CRUnitDTO(ChangeOperation.ADD, body));
            crs.getUnits().add(new CRUnitDTO(ChangeOperation.ADD, parcel));

            ChangeRequest<Store> request = vem.persist(crs);
            vem.publish(request);
            vem.affirm(request);
        });
        isolator.accept((vem) -> {
            ChangeRequestTemplate<Store> request = vem.em()
                    .createQuery("select r from StoreChangeRequest r where r.root.name = 'drugs'", StoreChangeRequest.class)
                    .getSingleResult();

            Assertions.assertEquals(2, vem.getChanger().stream(request, true).count());
        });
    }

    //todo
    /*@Test
    void testRemoveRequest() {
        isolator.accept((vem) -> {
            Store store = new Store("dixie");
            store.setBody(new StoreBody("urengoi"));
            store.getParcels().add(new Parcel("sweets"));

            ChangeRequest<Store> request = vem.persist(store);
            vem.publish(request);
            vem.reject(request);
        });
    }*/

    @Test
    void testDestroyChangeRequest() {
        String steadyUuid = UUID.randomUUID().toString();
        isolator.accept((vem) -> {
            Store store = new Store("dixie");
            store.setBody(new StoreBody("urengoi"));
            store.getParcels().add(new Parcel("sweets"));

            ChangeRequest<Store> request = vem.persist(store);
            vem.getSchema().datatype(request).primitive("uuid").set(request, steadyUuid);
            //request.setUuid(steadyUuid);
            vem.em().persist(request);
        });
        isolator.accept((vem) -> {
            ChangeRequest<Store> request = vem.find(StoreChangeRequest.class, steadyUuid);

            vem.destroy(request);
        });
        isolator.accept((vem) -> {
            Assertions.assertEquals(
                    0,
                    vem.em().createQuery("select s from Store s where s.name = :name")
                            .setParameter("name", "dixie")
                            .getResultList()
                            .size()
            );
            Assertions.assertEquals(
                    0,
                    vem.em().createQuery("select b from StoreBody b where b.address = :address")
                            .setParameter("address", "urengoi")
                            .getResultList()
                            .size()
            );
            Assertions.assertEquals(
                    0,
                    vem.em().createQuery("select p from Parcel p where p.name = :name")
                            .setParameter("name", "sweets")
                            .getResultList()
                            .size()
            );
            Assertions.assertEquals(
                    0,
                    vem.em().createQuery("select r from StoreChangeRequest r where r.uuid = :uuid")
                            .setParameter("uuid", steadyUuid)
                            .getResultList()
                            .size()
            );
        });
    }

    @Test
    void testChangeRequestAssociations() {
        isolator.accept((vem) -> {
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
            unit.setOperation(ChangeOperation.ADD);
            vem.em().persist(unit);
        });
        isolator.accept((vem) -> {
            StoreChangeRequest queriedRequest = vem.em()
                    .createQuery("select r from StoreChangeRequest r where r.root.name = 'ozon'", StoreChangeRequest.class)
                    .getSingleResult();

            Leaf<?> queriedLeaf = vem.getChanger().stream(queriedRequest, false).findFirst().get().getLeaf();

            Assertions.assertEquals("ozon", queriedRequest.getRoot().getName());
            Assertions.assertEquals("notebook", ((Parcel) queriedLeaf).getName());
        });
    }

    @Test
    void testMetadataCreation() {
        Schema schema = database.getVersioningEntityManagerFactory().getSchema();
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

    @Test
    void testParameterDatatype() {
        Datatype<Parcel> datatype = database.getVersioningEntityManagerFactory().getSchema().datatype(Parcel.class);

        Assertions.assertEquals(
                Item.class,
                datatype.collection("items").getParameterDatatype().getJavaType()
        );
        Assertions.assertEquals(
                Store.class,
                datatype.reference("parent").getParameterDatatype().getJavaType()
        );
        Assertions.assertNull(datatype.primitive("name").getParameterDatatype());
    }
}
