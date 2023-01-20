package lsa.prototype.vem.engine.impl.session;


import lsa.prototype.vem.model.context.ChangeOperation;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.EntityVersion;
import lsa.prototype.vem.model.version.LeafEntity;
import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.model.version.VersionedEntity;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;
import lsa.prototype.vem.spi.session.PersistenceProcessor;
import lsa.prototype.vem.spi.session.VersioningEntityManager;

import java.util.Set;
import java.util.UUID;

public class Persister implements PersistenceProcessor {
    private final static Set<EntityVersion.StateType> CHANGE_STATES = Set.of(EntityVersion.StateType.DRAFT, EntityVersion.StateType.PURGE);

    @Override
    public <T extends RootEntity, R extends ChangeRequest<T>, V extends VersionedEntity>
    void process(V oldEntity, V newEntity, R request, VersioningEntityManager vem) {
        Datatype<V> datatype = vem.getSchema().datatype(newEntity);
        UUID affinity = oldEntity.getUuid();

        for (Parameter<V> parameter : datatype.collections().values()) {
            for (LeafEntity<VersionedEntity> leaf : (Iterable<LeafEntity<VersionedEntity>>) parameter.get(newEntity)) {
                leaf.setAffinity(affinity);
                bind(request, leaf, vem);
                process(leaf, leaf, request, vem);
            }
        }

        for (Parameter<V> parameter : datatype.references().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            LeafEntity<VersionedEntity> leaf = (LeafEntity<VersionedEntity>) parameter.get(newEntity);
            if (leaf == null) {
                continue;
            }
            leaf.setAffinity(affinity);
            bind(request, leaf, vem);
        }
    }

    private <T extends RootEntity> void bind(ChangeRequest<T> request, LeafEntity<?> leaf, VersioningEntityManager vem) {
        if (CHANGE_STATES.contains(leaf.getVersion().getStateType())) {
            vem.em().persist(leaf);
            ChangeOperation operation = getOperation(leaf);

            ChangeUnit<ChangeRequest<T>> unit = vem.getChanger().createChangeUnit(request, leaf, operation);
            vem.em().persist(unit);
        }
    }

    private ChangeOperation getOperation(LeafEntity<?> leaf) {
        return switch (leaf.getVersion().getStateType()) {
            case DRAFT -> (leaf.getId() == 0)
                    ? ChangeOperation.ADD
                    : ChangeOperation.REPLACE;
            case PURGE -> ChangeOperation.REMOVE;
            default -> null;
        };
    }
}
