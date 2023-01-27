package io.persistence.vem.engine.impl.function;

import io.persistence.vem.domain.model.*;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.engine.impl.crs.CRSpecificationUnitDTO;
import io.persistence.vem.spi.function.Axis;
import io.persistence.vem.spi.function.VisitorContext;
import io.persistence.vem.spi.request.ChangeRequestSpecification;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.Parameter;
import io.persistence.vem.spi.session.VersioningEntityManager;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Util {
    public static <T extends Root, V extends Versionable>
    void defineChangeOperationCascade(V entity, VersioningEntityManager vem, ChangeRequestSpecification<T> specification, ChangeOperation operation) {
        Datatype<V> datatype = vem.getSchema().getDatatype(entity);
        Serializable parentUuid = vem.getSchema().getUtil().getUuid(entity);

        //collections
        for (Parameter<V> parameter : datatype.getCollections().values()) {
            Datatype<Leaf<?>> parameterDatatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();

            for (Leaf<?> leaf : (Iterable<Leaf<?>>) parameter.get(entity)) {
                specification.getUnits().add(new CRSpecificationUnitDTO(
                        operation,
                        leaf
                ));
                parameterDatatype.getPrimitive("parentUuid").set(leaf, parentUuid);
                defineChangeOperationCascade(
                        leaf,
                        vem,
                        specification,
                        operation
                );
            }
        }

        //references
        for (Parameter<V> parameter : datatype.getReferences().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            Leaf<Versionable> leaf = (Leaf<Versionable>) parameter.get(entity);
            if (leaf == null) {
                continue;
            }
            Datatype<Leaf<?>> parameterDatatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();
            specification.getUnits().add(new CRSpecificationUnitDTO(
                    operation,
                    leaf
            ));
            parameterDatatype.getPrimitive("parentUuid").set(leaf, parentUuid);
            defineChangeOperationCascade(
                    leaf,
                    vem,
                    specification,
                    operation
            );
        }
    }

    public static <V extends Persistable> void walk(V entity, VisitorContextImpl ctx, BiConsumer<Persistable, VisitorContext> task) {
        Datatype<V> datatype = ctx.vem().getSchema().getDatatype(entity);
        if (ctx.isVisited(entity))
            return;

        for (Parameter<V> parameter : datatype.getCollections().values()) {
            Axis<Persistable> axis = (Axis<Persistable>) new Axis<>(entity, parameter);
            for (Leaf<?> leaf : (Iterable<Leaf<?>>) parameter.get(entity)) {
                ctx.register(leaf, axis);
                walk(leaf, ctx, task);
            }
        }
        for (Parameter<V> parameter : datatype.getReferences().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            Axis<Persistable> axis = (Axis<Persistable>) new Axis<>(entity, parameter);
            Leaf<?> leaf = (Leaf<?>) parameter.get(entity);
            if (leaf != null) {
                ctx.register(leaf, axis);
                walk(leaf, ctx, task);
            }
        }
        task.accept(entity, ctx);
    }

    public static class VisitorContextImpl implements VisitorContext {
        private final VersioningEntityManager vem;
        private final Map<Persistable, Axis<Persistable>> register = new IdentityHashMap<>();

        public VisitorContextImpl(VersioningEntityManager vem) {
            this.vem = vem;
        }

        @Override
        public VersioningEntityManager vem() {
            return vem;
        }

        @Override
        public <T, U> Axis<U> getAxis(T entity) {
            return (Axis<U>) register.get(entity);
        }

        @Override
        public boolean isVisited(Persistable entity) {
            return register.containsKey(entity);
        }

        void register(Persistable entity, Axis<Persistable> axis) {
            register.put(entity, axis);
        }
    }

    //todo
    private static <T extends Root> void defCascade(VersioningEntityManager vem, T entity) {
        ChangeRequestSpecification<T> specification = null;
        ChangeOperation operation = null;

        vem.cascade(entity, (obj, ctx) -> {
            Axis<GlobalEntity> axis = ctx.getAxis(obj);
            Serializable parentUuid = (Serializable) axis.getParameter()
                    .getStructureDatatype()
                    .getGlobalIdentifier()
                    .get(axis.getParent());

            Parameter<?> parameter = ctx.getAxis(obj).getParameter();
            Datatype<Object> parameterDatatype = (Datatype<Object>) parameter.getParameterDatatype();
            specification.getUnits().add(new CRSpecificationUnitDTO(
                    operation,
                    (Leaf<?>) obj
            ));
            parameterDatatype.getPrimitive("parentUuid").set(obj, parentUuid);
        });
    }
}
