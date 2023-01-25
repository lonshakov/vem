package lsa.prototype.vem.engine.impl.lab;

import lsa.prototype.vem.engine.impl.request.CRUnitDTO;
import lsa.prototype.vem.model.Leaf;
import lsa.prototype.vem.model.Persistable;
import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.model.Versionable;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.spi.request.ChangeRequestSpecification;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Parameter;
import lsa.prototype.vem.spi.session.VersioningEntityManager;
import lsa.prototype.vem.spi.session.WalkContext;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CRSUtil {
    public static <T extends Root, V extends Versionable>
    void defineChangeOperationCascade(V entity, VersioningEntityManager vem, ChangeRequestSpecification<T> specification, ChangeOperation operation) {
        Datatype<V> datatype = vem.getSchema().datatype(entity);
        Serializable parentUuid = entity.getUuid();

        //collections
        for (Parameter<V> parameter : datatype.collections().values()) {
            Datatype<Leaf<?>> parameterDatatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();

            for (Leaf<?> leaf : (Iterable<Leaf<?>>) parameter.get(entity)) {
                specification.getUnits().add(new CRUnitDTO(
                        operation,
                        leaf
                ));
                parameterDatatype.primitive("parentUuid").set(leaf, parentUuid);
                defineChangeOperationCascade(
                        leaf,
                        vem,
                        specification,
                        operation
                );
            }
        }

        //references
        for (Parameter<V> parameter : datatype.references().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            Leaf<Versionable> leaf = (Leaf<Versionable>) parameter.get(entity);
            if (leaf == null) {
                continue;
            }
            Datatype<Leaf<?>> parameterDatatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();
            specification.getUnits().add(new CRUnitDTO(
                    operation,
                    leaf
            ));
            parameterDatatype.primitive("parentUuid").set(leaf, parentUuid);
            defineChangeOperationCascade(
                    leaf,
                    vem,
                    specification,
                    operation
            );
        }
    }

    public static <V extends Persistable> void walk(V entity, WalkContextImpl ctx, BiConsumer<Persistable, WalkContext> task) {
        Datatype<V> datatype = ctx.vem().getSchema().datatype(entity);
        if (ctx.isWalked(entity))
            return;

        for (Parameter<V> parameter : datatype.collections().values()) {
            for (Leaf<?> leaf : (Iterable<Leaf<?>>) parameter.get(entity)) {
                ctx.register(leaf, entity);
                walk(leaf, ctx, task);
            }
        }
        for (Parameter<V> parameter : datatype.references().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            Leaf<?> leaf = (Leaf<?>) parameter.get(entity);
            if (leaf != null) {
                ctx.register(leaf, entity);
                walk(leaf, ctx, task);
            }
        }
        task.accept(entity, ctx);
    }

    public static class WalkContextImpl implements WalkContext {
        private final VersioningEntityManager vem;
        private final Map<Persistable, Persistable> done = new IdentityHashMap<>();

        public WalkContextImpl(VersioningEntityManager vem) {
            this.vem = vem;
        }

        @Override
        public VersioningEntityManager vem() {
            return vem;
        }

        @Override
        public Persistable getParent(Persistable entity) {
            return done.get(entity);
        }

        @Override
        public boolean isWalked(Persistable entity) {
            return done.containsKey(entity);
        }

        void register(Persistable entity, Persistable parent) {
            done.put(entity, parent);
        }
    }
}
