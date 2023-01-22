package lsa.prototype.vem.engine.impl.session;


import lsa.prototype.vem.model.Leaf;
import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.model.VersionState;
import lsa.prototype.vem.model.Versionable;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.request.ChangeRequest;
import lsa.prototype.vem.request.ChangeUnit;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;
import lsa.prototype.vem.spi.session.PersistenceProcessor;
import lsa.prototype.vem.spi.session.VersioningEntityManager;

import java.io.Serializable;
import java.util.Set;

public class Persister implements PersistenceProcessor {
    private final static Set<VersionState> CHANGE_STATES = Set.of(VersionState.DRAFT, VersionState.PURGE);

    @Override
    public <T extends Root, R extends ChangeRequest<T>, V extends Versionable>
    void process(V oldEntity, V newEntity, R request, VersioningEntityManager vem) {
        Datatype<V> datatype = vem.getSchema().datatype(newEntity);
        Serializable parentUuid = oldEntity.getUuid();

        for (Parameter<V> parameter : datatype.collections().values()) {
            for (Leaf<Versionable> leaf : (Iterable<Leaf<Versionable>>) parameter.get(newEntity)) {
                vem.getSchema().datatype(leaf).primitive("parentUuid").set(leaf, parentUuid);
                bind(request, leaf, vem);
                process(leaf, leaf, request, vem);
            }
        }

        for (Parameter<V> parameter : datatype.references().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            Leaf<Versionable> leaf = (Leaf<Versionable>) parameter.get(newEntity);
            if (leaf == null) {
                continue;
            }
            //leaf.setParentUuid(parentUuid);
            vem.getSchema().datatype(leaf).primitive("parentUuid").set(leaf, parentUuid);
            bind(request, leaf, vem);
        }
    }

    private <T extends Root> void bind(ChangeRequest<T> request, Leaf<?> leaf, VersioningEntityManager vem) {
        if (CHANGE_STATES.contains(leaf.getVersion().getState())) {
            vem.em().persist(leaf);
            ChangeOperation operation = getOperation(leaf);

            ChangeUnit<ChangeRequest<T>> unit = vem.getChanger().createChangeUnit(request, leaf, operation);
            vem.em().persist(unit);
        }
    }

    private ChangeOperation getOperation(Leaf<?> leaf) {
        return switch (leaf.getVersion().getState()) {
            case DRAFT -> (leaf.getId() == (Serializable) 0)
                    ? ChangeOperation.ADD
                    : ChangeOperation.REPLACE;
            case PURGE -> ChangeOperation.REMOVE;
            default -> null;
        };
    }
}
