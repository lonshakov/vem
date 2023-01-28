package io.persistence.vem.engine.impl.function;

import io.persistence.vem.domain.model.*;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.domain.request.ChangeRequest;
import io.persistence.vem.spi.context.SessionContext;
import io.persistence.vem.spi.function.HistoryRecorder;
import io.persistence.vem.spi.request.Changer;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.Schema;
import io.persistence.vem.spi.session.VersioningEntityManager;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.time.LocalDateTime;

public class HistoryRecorderImpl implements HistoryRecorder {
    private final EntityManager em;
    private final Schema schema;
    private final Changer changer;
    private final SessionContext context;

    public HistoryRecorderImpl(VersioningEntityManager vem) {
        this.em = vem.em();
        this.schema = vem.getSchema();
        this.changer = vem.getChanger();
        this.context = vem.getSessionContext();
    }

    public <T extends Root> void record(ChangeRequest<T> request) {
        LocalDateTime dateTime = LocalDateTime.now();

        markRootEntity(request, dateTime);

        changer.stream(request, false).forEach(unit -> {
            markHistory(
                    unit.getOperation(),
                    unit.getLeaf(),
                    dateTime
            );
            markActual(
                    unit.getOperation(),
                    unit.getLeaf(),
                    dateTime
            );
        });
    }

    private <T extends Root> void markRootEntity(ChangeRequest<T> request, LocalDateTime dateTime) {
        T root = request.getRoot();
        if (root.getVersion().getState().equals(VersionState.DRAFT)) {
            Datatype<T> datatype = schema.getDatatype(root);

            Version version = new Version(VersionState.ACTIVE, context.getUser().getLogin());
            Lifetime lifetime = new Lifetime(dateTime, dateTime.plusYears(10));

            datatype.getPrimitive("version").set(root, version);
            datatype.getPrimitive("lifetime").set(root, lifetime);

            em.merge(request.getRoot());
        }
    }

    private <T extends Leaf<P>, P extends Versionable> void markHistory(ChangeOperation operation, T leaf, LocalDateTime dateTime) {
        Datatype<T> datatype = schema.getDatatype(leaf);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<T> type = datatype.getJavaType();

        CriteriaQuery<T> query = cb.createQuery(type);
        javax.persistence.criteria.Root<T> root = query.from(type);

        Version version = new Version(VersionState.HISTORY, context.getUser().getLogin());
        Lifetime lifetime = new Lifetime(leaf.getLifetime().getStarting(), dateTime);

        //set history state
        switch (operation) {
            case COLLECTION_REMOVE -> {
                //find oneself
                query.select(root).where(
                        cb.equal(root.get(datatype.getGlobalIdentifier().getName()), schema.getUtil().getUuid(leaf)),
                        cb.equal(root.get("version").get("state"), VersionState.ACTIVE)
                );
                em.createQuery(query).getResultList().forEach(active -> {
                    datatype.getPrimitive("version").set(active, version);
                    datatype.getPrimitive("lifetime").set(active, lifetime);
                    datatype.getReference("parent").set(active, null);
                    em.merge(active);
                });
            }
            case COLLECTION_ADD, CASCADE_CREATE -> {
                //NOOP
            }
            case REFERENCE_REPLACE, REFERENCE_NULLIFY, CASCADE_DELETE -> {
                //find siblings
                query.select(root).where(
                        cb.equal(root.get("parentUuid"), leaf.getParentUuid()),
                        cb.equal(root.get("version").get("state"), VersionState.ACTIVE)
                );
                em.createQuery(query).getResultList().forEach(active -> {
                    datatype.getPrimitive("version").set(active, version);
                    datatype.getPrimitive("lifetime").set(active, lifetime);
                    datatype.getReference("parent").set(active, null);
                    em.merge(active);
                });
            }
        }
    }

    private <T extends Leaf<P>, P extends Versionable> void markActual(ChangeOperation operation, T leaf, LocalDateTime dateTime) {
        Datatype<T> datatype = schema.getDatatype(leaf);
        //set active/passive state
        VersionState state = switch (operation) {
            case COLLECTION_ADD, REFERENCE_REPLACE, CASCADE_CREATE -> VersionState.ACTIVE;
            case COLLECTION_REMOVE, REFERENCE_NULLIFY, CASCADE_DELETE -> VersionState.PASSIVE;
        };
        Version version = new Version(state, context.getUser().getLogin());
        Lifetime lifetime = new Lifetime(dateTime, dateTime.plusYears(10));

        datatype.getPrimitive("version").set(leaf, version);
        datatype.getPrimitive("lifetime").set(leaf, lifetime);

        em.merge(leaf);
    }
}
