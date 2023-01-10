package lsa.prototype.vem.engine.impl;

import jakarta.persistence.EntityManager;
import lsa.prototype.vem.engine.spi.VersioningEntityManager;
import lsa.prototype.vem.engine.spi.VersioningEntityManagerFactory;
import lsa.prototype.vem.engine.spi.meta.Datatype;
import lsa.prototype.vem.engine.spi.meta.Parameter;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.Leaf;
import lsa.prototype.vem.model.version.Root;
import lsa.prototype.vem.model.version.VersionedEntity;

//TODO
public class HibernateVersioningSession implements VersioningEntityManager {
    private final VersioningEntityManagerFactory factory;
    private final EntityManager em;

    public HibernateVersioningSession(VersioningEntityManagerFactory factory, EntityManager em) {
        this.factory = factory;
        this.em = em;
    }

    @Override
    public <T extends Root, R extends ChangeRequest<T>> R persist(T entity) {
        R request = (R) factory().getHistoryMapping().get(entity).request().instantiate();

        em.persist(request);
        em.persist(entity);
        request.setRoot(entity);

        walk(entity, request);

        return request;
    }

    @Override
    public <T extends Root, R extends ChangeRequest<T>> R merge(T entity) {
        R request = (R) factory().getHistoryMapping().get(entity).request().instantiate();
        T storedEntity = em.find((Class<T>) entity.getClass(), entity.getId());

        em.persist(request);
        request.setRoot(storedEntity);

        //todo

        return request;
    }

    @Override
    public <T extends Root, R extends ChangeRequest<T>> R remove(T entity) {
        return null;
    }

    @Override
    public EntityManager em() {
        return em;
    }

    @Override
    public VersioningEntityManagerFactory factory() {
        return factory;
    }

    private <T extends Root, R extends ChangeRequest<T>, V extends VersionedEntity> void walk(V entity, R request) {
        Datatype<V> datatype = meta().datatype(entity);

        for (Parameter<V> parameter : datatype.collections().values()) {
            for (Leaf<VersionedEntity> leaf : (Iterable<Leaf<VersionedEntity>>) parameter.get(entity)) {
                leaf.setParent(entity);
                bind(request, leaf);
                walk(leaf, request);
            }
        }

        for (Parameter<V> parameter : datatype.references().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            Leaf<VersionedEntity> leaf = (Leaf<VersionedEntity>) parameter.get(entity);
            if (leaf == null) {
                continue;
            }
            bind(request, leaf);
        }
    }


    private <T extends Root, R extends ChangeRequest<T>> void bind(R request, Leaf<?> leaf) {
        ChangeUnit<T> unit = (ChangeUnit<T>) factory().getHistoryMapping().get(leaf).unit().instantiate();
        unit.setRequest(request);
        unit.setLeaf(leaf);
        em.persist(leaf);
        em.persist(unit);
    }

    @Override
    public void close() {
        em.close();
    }
}
