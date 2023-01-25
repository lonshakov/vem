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

public class Persister implements PersistenceProcessor {
    @Override
    public <T extends Root, R extends ChangeRequest<T>, V extends Versionable>
    void process(V entity, R request, VersioningEntityManager vem) {
        Datatype<V> datatype = vem.getSchema().datatype(entity);
        Serializable parentUuid = entity.getUuid();

        for (Parameter<V> parameter : datatype.collections().values()) {
            for (Leaf<Versionable> leaf : (Iterable<Leaf<Versionable>>) parameter.get(entity)) {
                vem.getSchema().datatype(leaf).primitive("parentUuid").set(leaf, parentUuid);
                bind(request, leaf, vem);
                process(leaf, request, vem);
            }
        }

        for (Parameter<V> parameter : datatype.references().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            Leaf<Versionable> leaf = (Leaf<Versionable>) parameter.get(entity);
            if (leaf == null) {
                continue;
            }
            vem.getSchema().datatype(leaf).primitive("parentUuid").set(leaf, parentUuid);
            bind(request, leaf, vem);
            process(leaf, request, vem);
        }
    }

    private <T extends Root> void bind(ChangeRequest<T> request, Leaf<?> leaf, VersioningEntityManager vem) {
        if (VersionState.DRAFT.equals(leaf.getVersion().getState())) {
            ChangeOperation operation = getOperation(leaf);
            vem.em().persist(leaf);

            ChangeUnit<ChangeRequest<T>> unit = vem.getChanger().createChangeUnit(request, leaf, operation);
            vem.em().persist(unit);
        }
    }

    private ChangeOperation getOperation(Leaf<?> leaf) {
        if (leaf.getId() == null) {
            return ChangeOperation.ADD;
        }
        if (leaf.getParent() == null && leaf.getParentUuid() != null) {
            return ChangeOperation.REMOVE;
        }
        return ChangeOperation.REPLACE;
    }
}
