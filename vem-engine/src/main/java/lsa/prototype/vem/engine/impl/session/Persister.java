package lsa.prototype.vem.engine.impl.session;


import lsa.prototype.vem.model.EntityVersion;
import lsa.prototype.vem.model.ILeafEntity;
import lsa.prototype.vem.model.IRootEntity;
import lsa.prototype.vem.model.IVersionedEntity;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.request.IChangeRequest;
import lsa.prototype.vem.request.IChangeUnit;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;
import lsa.prototype.vem.spi.session.PersistenceProcessor;
import lsa.prototype.vem.spi.session.VersioningEntityManager;

import java.io.Serializable;
import java.util.Set;

public class Persister implements PersistenceProcessor {
    private final static Set<EntityVersion.StateType> CHANGE_STATES = Set.of(EntityVersion.StateType.DRAFT, EntityVersion.StateType.PURGE);

    @Override
    public <T extends IRootEntity, R extends IChangeRequest<T>, V extends IVersionedEntity>
    void process(V oldEntity, V newEntity, R request, VersioningEntityManager vem) {
        Datatype<V> datatype = vem.getSchema().datatype(newEntity);
        Serializable parentUuid = oldEntity.getUuid();

        for (Parameter<V> parameter : datatype.collections().values()) {
            for (ILeafEntity<IVersionedEntity> leaf : (Iterable<ILeafEntity<IVersionedEntity>>) parameter.get(newEntity)) {
                leaf.setParentUuid(parentUuid);
                bind(request, leaf, vem);
                process(leaf, leaf, request, vem);
            }
        }

        for (Parameter<V> parameter : datatype.references().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            ILeafEntity<IVersionedEntity> leaf = (ILeafEntity<IVersionedEntity>) parameter.get(newEntity);
            if (leaf == null) {
                continue;
            }
            leaf.setParentUuid(parentUuid);
            bind(request, leaf, vem);
        }
    }

    private <T extends IRootEntity> void bind(IChangeRequest<T> request, ILeafEntity<?> leaf, VersioningEntityManager vem) {
        if (CHANGE_STATES.contains(leaf.getVersion().getStateType())) {
            vem.em().persist(leaf);
            ChangeOperation operation = getOperation(leaf);

            IChangeUnit<IChangeRequest<T>> unit = vem.getChanger().createChangeUnit(request, leaf, operation);
            vem.em().persist(unit);
        }
    }

    private ChangeOperation getOperation(ILeafEntity<?> leaf) {
        return switch (leaf.getVersion().getStateType()) {
            case DRAFT -> (leaf.getId() == (Serializable) 0)
                    ? ChangeOperation.ADD
                    : ChangeOperation.REPLACE;
            case PURGE -> ChangeOperation.REMOVE;
            default -> null;
        };
    }
}
