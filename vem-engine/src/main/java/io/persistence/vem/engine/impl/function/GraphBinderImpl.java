package io.persistence.vem.engine.impl.function;

import io.persistence.vem.domain.model.Leaf;
import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.model.VersionState;
import io.persistence.vem.domain.model.Versionable;
import io.persistence.vem.domain.request.ChangeRequest;
import io.persistence.vem.spi.function.GraphBinder;
import io.persistence.vem.spi.request.Changer;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.Parameter;
import io.persistence.vem.spi.schema.Schema;
import io.persistence.vem.spi.session.VersioningEntityManager;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

public class GraphBinderImpl implements GraphBinder {
    private final EntityManager em;
    private final Schema schema;
    private final Changer changer;

    public GraphBinderImpl(VersioningEntityManager vem) {
        this.em = vem.em();
        this.schema = vem.getSchema();
        this.changer = vem.getChanger();
    }

    @Override
    public <T extends Root> void bind(ChangeRequest<T> request) {
        changer.stream(request, true).forEach(unit ->
                processGraphBinding(unit.getLeaf())
        );
    }

    private <T extends Leaf<P>, P extends Versionable> void processGraphBinding(T leaf) {
        Versionable parent = leaf.getVersion().getState().equals(VersionState.ACTIVE)
                ? em.createQuery(getActiveParentQuery(leaf)).getSingleResult()
                : null;
        schema.getDatatype(leaf).getReference("parent").set(leaf, parent);
    }

    private <T extends Leaf<P>, P extends Versionable> CriteriaQuery<P> getActiveParentQuery(T entity) {
        Datatype<T> datatype = schema.getDatatype(entity);
        Parameter<T> parent = datatype.getReference("parent");

        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<P> type = (Class<P>) parent.getParameterDatatype().getJavaType();

        CriteriaQuery<P> query = cb.createQuery(type);
        javax.persistence.criteria.Root<P> root = query.from(type);

        query.select(root).where(
                cb.equal(root.get(datatype.getGlobalIdentifier().getName()), entity.getParentUuid()),
                cb.equal(root.get("version").get("state"), VersionState.ACTIVE)
        );

        return query;
    }
}
