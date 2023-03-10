package io.persistence.vem.engine.impl.function;


import io.persistence.vem.domain.model.Leaf;
import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.model.VersionState;
import io.persistence.vem.domain.model.Versionable;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.domain.request.ChangeRequest;
import io.persistence.vem.domain.request.ChangeUnit;
import io.persistence.vem.spi.function.PersistenceProcessor;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.PluralParameter;
import io.persistence.vem.spi.schema.SingularParameter;
import io.persistence.vem.spi.session.VersioningEntityManager;

import java.io.Serializable;

public class Persister implements PersistenceProcessor {
    @Override
    public <T extends Root, R extends ChangeRequest<T>, V extends Versionable>
    void process(V entity, R request, VersioningEntityManager vem) {
        Datatype<V> datatype = vem.getSchema().getDatatype(entity);
        Serializable parentUuid = vem.getSchema().getUtil().getUuid(entity);

        for (PluralParameter<V> parameter : datatype.getCollections().values()) {
            parameter.get(entity).stream().map(obj -> (Leaf<?>) obj).forEach(leaf -> {
                vem.getSchema().getDatatype(leaf).getPrimitive("parentUuid").set(leaf, parentUuid);
                bind(request, leaf, vem);
                process(leaf, request, vem);
            });
        }

        for (SingularParameter<V> parameter : datatype.getReferences().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            Leaf<Versionable> leaf = parameter.get(entity);
            if (leaf == null) {
                continue;
            }
            vem.getSchema().getDatatype(leaf).getPrimitive("parentUuid").set(leaf, parentUuid);
            bind(request, leaf, vem);
            process(leaf, request, vem);
        }
    }

    private <T extends Root> void bind(ChangeRequest<T> request, Leaf<?> leaf, VersioningEntityManager vem) {
        if (VersionState.DRAFT.equals(leaf.getVersion().getState())) {
            ChangeOperation operation = getOperation(leaf, vem);
            vem.em().persist(leaf);

            ChangeUnit<ChangeRequest<T>> unit = vem.getChanger().createChangeUnit(request, leaf, operation);
            vem.em().persist(unit);
        }
    }

    private ChangeOperation getOperation(Leaf<?> leaf, VersioningEntityManager vem) {
        if (vem.getSchema().getUtil().getId(leaf) == null) {
            return ChangeOperation.COLLECTION_ADD;
        }
        if (leaf.getParent() == null && leaf.getParentUuid() != null) {
            return ChangeOperation.COLLECTION_REMOVE;
        }
        return ChangeOperation.REFERENCE_REPLACE;
    }
}
