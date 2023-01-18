package lsa.prototype.vem.engine.impl;


import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.context.PolymorphEntity;
import lsa.prototype.vem.model.version.EntityVersion;
import lsa.prototype.vem.model.version.Leaf;
import lsa.prototype.vem.model.version.Root;
import lsa.prototype.vem.model.version.VersionedEntity;
import lsa.prototype.vem.spi.PersistenceProcessor;
import lsa.prototype.vem.spi.VersioningEntityManager;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;

import java.util.UUID;

public class Persister implements PersistenceProcessor {
    @Override
    public <T extends Root, R extends ChangeRequest<T>, V extends VersionedEntity>
    void process(V oldEntity, V newEntity, R request, VersioningEntityManager vem) {
        Datatype<V> datatype = vem.getSchema().datatype(newEntity);

        for (Parameter<V> parameter : datatype.collections().values()) {
            for (Leaf<VersionedEntity> leaf : (Iterable<Leaf<VersionedEntity>>) parameter.get(newEntity)) {
                UUID affinity = oldEntity.getUuid();
                VersionedEntity parent = EntityVersion.StateType.ACTIVE.equals(leaf.getVersion().getStateType())
                        ? oldEntity
                        : null;

                leaf.setParent(parent);
                leaf.setAffinity(affinity);

                bind(request, leaf, vem);
                process(leaf, leaf, request, vem);
            }
        }

        for (Parameter<V> parameter : datatype.references().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            Leaf<VersionedEntity> leaf = (Leaf<VersionedEntity>) parameter.get(newEntity);
            if (leaf == null) {
                continue;
            }
            leaf.setParent(oldEntity);
            bind(request, leaf, vem);
        }
    }

    private <T extends Root, R extends ChangeRequest<T>> void bind(R request, Leaf<?> leaf, VersioningEntityManager vem) {
        ChangeUnit<R> unit = (ChangeUnit<R>) vem.getFactory().getHistoryMapping().get(leaf).getUnitDatatype().instantiate();
        unit.setRequest(request);
        vem.em().persist(leaf);
        unit.setLeaf(new PolymorphEntity(leaf));
        vem.em().persist(unit);
    }
}
